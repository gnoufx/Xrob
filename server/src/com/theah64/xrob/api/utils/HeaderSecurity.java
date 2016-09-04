package com.theah64.xrob.api.utils;

import com.theah64.xrob.api.database.tables.Users;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by shifar on 31/12/15.
 */
public final class HeaderSecurity {

    public static final String KEY_AUTHORIZATION = "Authorization";
    private static final String REASON_API_KEY_MISSING = "API key is missing";
    private static final String REASON_INVALID_API_KEY = "Invalid API key";
    private final String authorization;
    private String userId;

    public HeaderSecurity(final String authorization) throws Exception {
        //Collecting header from passed request
        this.authorization = authorization;
        isAuthorized();
    }

    /**
     * Used to identify if passed API-KEY has a valid user.
     */
    private void isAuthorized() throws Exception {

        if (this.authorization == null) {
            //No api key passed along with request
            throw new Exception("Unauthorized access");
        }

        final Users users = Users.getInstance();
        this.userId = users.get(Users.COLUMN_API_KEY, this.authorization, Users.COLUMN_ID);
        if (this.userId == null) {
            throw new Exception("No user found with the api_key " + this.authorization);
        }

    }

    public String getUserId() {
        return this.userId;
    }

    public String getFailureReason() {
        return this.authorization == null ? REASON_API_KEY_MISSING : REASON_INVALID_API_KEY;
    }
}
