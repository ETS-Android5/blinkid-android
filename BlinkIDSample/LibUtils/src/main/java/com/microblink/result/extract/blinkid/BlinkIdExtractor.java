/*
 * Copyright (c)2020 Microblink Ltd. All rights reserved.
 *
 * ANY UNAUTHORIZED USE OR SALE, DUPLICATION, OR DISTRIBUTION
 * OF THIS PROGRAM OR ANY OF ITS PARTS, IN SOURCE OR BINARY FORMS,
 * WITH OR WITHOUT MODIFICATION, WITH THE PURPOSE OF ACQUIRING
 * UNLAWFUL MATERIAL OR ANY OTHER BENEFIT IS PROHIBITED!
 * THIS PROGRAM IS PROTECTED BY COPYRIGHT LAWS AND YOU MAY NOT
 * REVERSE ENGINEER, DECOMPILE, OR DISASSEMBLE IT.
 */

package com.microblink.result.extract.blinkid;

import com.microblink.entities.recognizers.Recognizer;
import com.microblink.entities.recognizers.blinkid.generic.DriverLicenseDetailedInfo;
import com.microblink.entities.recognizers.blinkid.generic.VehicleClassInfo;
import com.microblink.entities.recognizers.blinkid.imageresult.EncodedFaceImageResult;
import com.microblink.entities.recognizers.blinkid.imageresult.EncodedFullDocumentImageResult;
import com.microblink.entities.recognizers.blinkid.imageresult.EncodedSignatureImageResult;
import com.microblink.entities.recognizers.blinkid.imageresult.FaceImageResult;
import com.microblink.entities.recognizers.blinkid.imageresult.FullDocumentImageResult;
import com.microblink.entities.recognizers.blinkid.imageresult.SignatureImageResult;
import com.microblink.entities.recognizers.blinkid.mrtd.MrtdDocumentType;
import com.microblink.entities.recognizers.blinkid.mrtd.MrzResult;
import com.microblink.libutils.R;
import com.microblink.result.extract.BaseResultExtractor;
import com.microblink.result.extract.RecognitionResultEntry;
import com.microblink.result.extract.util.images.CombinedFullDocumentImagesExtractUtil;
import java.util.List;

public abstract class BlinkIdExtractor<ResultType extends Recognizer.Result, RecognizerType extends Recognizer<ResultType>> extends BaseResultExtractor<ResultType, RecognizerType> {

    @Override
    protected void onDataExtractionDone(ResultType result) {
        extractCommonData(result, mExtractedData, mBuilder);
        super.onDataExtractionDone(result);
    }

    protected void extractMRZResult(MrzResult mrzResult) {
        MrtdDocumentType docType = mrzResult.getDocumentType();

        add(R.string.PPDocumentType, docType.toString());
        add(R.string.PPMRZParsed, mrzResult.isMrzParsed());
        add(R.string.PPMRZVerified, mrzResult.isMrzVerified());
        add(R.string.PPPrimaryId, mrzResult.getPrimaryId());
        add(R.string.PPSecondaryId, mrzResult.getSecondaryId());
        add(R.string.PPDateOfBirth, mrzResult.getDateOfBirth().getDate());
        int age = mrzResult.getAge();
        if (age != -1) {
            add(R.string.PPAge, age);
        }
        add(R.string.PPSex, mrzResult.getGender());
        add(R.string.PPNationalityCode, mrzResult.getSanitizedNationality());
        add(R.string.PPNationality, mrzResult.getNationalityName());
        add(R.string.PPDocumentCode, mrzResult.getSanitizedDocumentCode());
        add(R.string.PPIssuerCode, mrzResult.getSanitizedIssuer());
        add(R.string.PPIssuer, mrzResult.getIssuerName());
        add(R.string.PPDateOfExpiry, mrzResult.getDateOfExpiry().getDate());
        add(R.string.PPOpt2, mrzResult.getSanitizedOpt2());
        add(R.string.PPMRZText, mrzResult.getMrzText());

        if (docType == MrtdDocumentType.MRTD_TYPE_GREEN_CARD) {
            add(R.string.PPAlienNumber, mrzResult.getAlienNumber());
            add(R.string.PPApplicationReceiptNumber, mrzResult.getApplicationReceiptNumber());
            add(R.string.PPImmigrantCaseNumber, mrzResult.getImmigrantCaseNumber());
        } else {
            add(R.string.PPDocumentNumber, mrzResult.getSanitizedDocumentNumber());
            add(R.string.PPOpt1, mrzResult.getSanitizedOpt1());
        }
    }

    protected void extractCommonData(Recognizer.Result result,
                                     List<RecognitionResultEntry> extractedData,
                                     RecognitionResultEntry.Builder builder) {
        if(result instanceof FaceImageResult) {
            extractedData.add(builder.build(R.string.MBFaceImage, ((FaceImageResult) result).getFaceImage()));
        }

        if (result instanceof EncodedFaceImageResult) {
            byte[] encodedFaceImage = ((EncodedFaceImageResult) result).getEncodedFaceImage();
            if (shouldShowEncodedImageEntry(encodedFaceImage)) {
                extractedData.add(builder.build(R.string.MBEncodedFaceImage, encodedFaceImage));
            }
        }

        if(result instanceof FullDocumentImageResult) {
            extractedData.add(builder.build(R.string.MBFullDocumentImage, ((FullDocumentImageResult) result).getFullDocumentImage()));
        }

        if (result instanceof EncodedFullDocumentImageResult) {
            byte[] encodedFullDocumentImage = ((EncodedFullDocumentImageResult) result).getEncodedFullDocumentImage();
            if (shouldShowEncodedImageEntry(encodedFullDocumentImage)) {
                extractedData.add(builder.build(R.string.MBEncodedFullDocumentImage, encodedFullDocumentImage));
            }
        }

        CombinedFullDocumentImagesExtractUtil.extractCombinedFullDocumentImages(result, extractedData, builder);

        if(result instanceof SignatureImageResult) {
            extractedData.add(builder.build(R.string.MBSignatureImage, ((SignatureImageResult) result).getSignatureImage()));
        }

        if (result instanceof EncodedSignatureImageResult) {
            byte[] encodedSignatureImage = ((EncodedSignatureImageResult) result).getEncodedSignatureImage();
            if (shouldShowEncodedImageEntry(encodedSignatureImage)) {
                extractedData.add(builder.build(R.string.MBEncodedSignatureImage, encodedSignatureImage));
            }
        }
    }

    protected static boolean shouldShowEncodedImageEntry(byte[] encodedImage) {
        return encodedImage != null && encodedImage.length > 0;
    }

    protected String extractVehicleClassesInfo(DriverLicenseDetailedInfo driverLicenseInfo) {
        StringBuilder vehicleClassInfoResult = new StringBuilder();
        for (VehicleClassInfo vehicleClassInfo : driverLicenseInfo.getVehicleClassesInfo()) {
            vehicleClassInfoResult.append(vehicleClassInfo.getVehicleClass()).append(" ")
                    .append(vehicleClassInfo.getLicenceType()).append(" ")
                    .append(vehicleClassInfo.getEffectiveDate().getOriginalDateString()).append(" ")
                    .append(vehicleClassInfo.getExpiryDate().getOriginalDateString())
                    .append("\n");
        }
        return vehicleClassInfoResult.toString();
    }

}
