package com.uit.buddy.constant;

public final class MoodleApiConstants {

    private MoodleApiConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Function names
    public static final String FUNCTION_GET_SITE_INFO = "core_webservice_get_site_info";
    public static final String FUNCTION_GET_USERS_COURSES = "core_enrol_get_users_courses";

    // Parameter keys
    public static final String PARAM_WSTOKEN = "wstoken";
    public static final String PARAM_WSFUNCTION = "wsfunction";
    public static final String PARAM_MOODLEWSRESTFORMAT = "moodlewsrestformat";

}
