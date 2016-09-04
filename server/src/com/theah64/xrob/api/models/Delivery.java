package com.theah64.xrob.api.models;

import javax.servlet.ServletContext;
import java.io.File;

/**
 * Created by theapache64 on 11/29/2015.
 */
public class Delivery {

    public static final String TYPE_MESSAGES = "messages";

    private static final String TYPE_CALL_LOGS = "call_logs";

    private static final String TYPE_CONTACTS = "contacts";
    private static final String TYPE_FILES = "files";
    private static final String TYPE_MEDIA_SCREEN_SHOT = "media_screen_shot";
    private static final String TYPE_MEDIA_VOICE = "media_voice";
    private static final String TYPE_MEDIA_SELFIE = "media_selfie";

    private final String id, userId, message, createdAt;
    private String dataType;
    private final boolean hasError;

    private Delivery(String userId, boolean hasError, String message, String dataType, String id, String createdAt) throws Exception {
        this.id = id;
        this.userId = userId;
        this.dataType = dataType;
        this.message = message;
        this.hasError = hasError;
        this.createdAt = createdAt;

        checkDataType();
    }

    public Delivery(String userId, boolean hasError, String message, final String dataType) throws Exception {
        this(userId, hasError, message, dataType, null, null);
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    private void checkDataType() throws Exception {

        switch (this.dataType) {

            case TYPE_MESSAGES:
            case TYPE_CALL_LOGS:
            case TYPE_CONTACTS:
            case TYPE_FILES:
            case TYPE_MEDIA_SCREEN_SHOT:
            case TYPE_MEDIA_VOICE:
            case TYPE_MEDIA_SELFIE:
                break;

            default:
                //Undefined data type
                throw new Exception("Invalid data type " + this.dataType);
        }

    }

    public String getUserId() {
        return userId;
    }

    public boolean hasError() {
        return hasError;
    }

    public String getMessage() {
        return message;
    }

    public String getDataType() {
        return dataType;
    }

    public static class Type {

        private static final String BASE_FILE_STORAGE_FOLDER_NAME = "uploads";
        private static final String FILE_STORAGE_FOLDER_NAME = "files";
        private static final String SCREENSHOTS_STORAGE_FOLDER_NAME = "screenshots";
        private static final String SELFIES_STORAGE_FOLDER_NAME = "selfies";
        private static final String VOICES_STORAGE_FOLDER_NAME = "voices";
        private static String VOICES_STORAGE_PATH;
        private static String SELFIES_STORAGE_PATH;
        private static String SCREENSHOTS_STORAGE_PATH;
        private static String FILES_STORAGE_PATH;
        private static String baseFileStoragePath;

        private final String dataType;

        public Type(final ServletContext context, String dataType) {

            this.dataType = dataType;

            //Initializing base file storage path for the first time.
            if (baseFileStoragePath == null) {
                baseFileStoragePath = context.getRealPath(BASE_FILE_STORAGE_FOLDER_NAME);

                initOtherStoragePaths(baseFileStoragePath);
            }
        }


        public boolean isValid() {

            switch (this.dataType) {

                case TYPE_MESSAGES:
                case TYPE_CALL_LOGS:
                case TYPE_CONTACTS:
                case TYPE_FILES:
                case TYPE_MEDIA_SCREEN_SHOT:
                case TYPE_MEDIA_VOICE:
                case TYPE_MEDIA_SELFIE:
                    return true;

                default:
                    //Undefined data type
                    return false;
            }

        }

        /**
         * @return true if the current dataType is binary, otherwise false.
         */
        public boolean isBinary() {
            switch (this.dataType) {

                case TYPE_FILES:
                case TYPE_MEDIA_SCREEN_SHOT:
                case TYPE_MEDIA_SELFIE:
                case TYPE_MEDIA_VOICE:
                    return true;

                case TYPE_CONTACTS:
                case TYPE_MESSAGES:
                case TYPE_CALL_LOGS:
                    return false;

                default:
                    throw new IllegalArgumentException(this.dataType + " data type is not managed.");
            }
        }

        /**
         * Initialize the storage paths of the all binary data types.
         *
         * @param baseFileStoragePath Base path
         */
        private static void initOtherStoragePaths(String baseFileStoragePath) {
            FILES_STORAGE_PATH = baseFileStoragePath + File.separator + FILE_STORAGE_FOLDER_NAME;
            SCREENSHOTS_STORAGE_PATH = baseFileStoragePath + File.separator + SCREENSHOTS_STORAGE_FOLDER_NAME;
            SELFIES_STORAGE_PATH = baseFileStoragePath + File.separator + SELFIES_STORAGE_FOLDER_NAME;
            VOICES_STORAGE_PATH = baseFileStoragePath + File.separator + VOICES_STORAGE_FOLDER_NAME;
        }

        /**
         * @return storage path for the current data type.
         */
        public String getStoragePath() {
            switch (this.dataType) {
                case TYPE_FILES:
                    return FILES_STORAGE_PATH;
                case TYPE_MEDIA_SCREEN_SHOT:
                    return SCREENSHOTS_STORAGE_PATH;
                case TYPE_MEDIA_SELFIE:
                    return SELFIES_STORAGE_PATH;
                case TYPE_MEDIA_VOICE:
                    return VOICES_STORAGE_PATH;
                default:
                    return null;
            }
        }
    }
}
