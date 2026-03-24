package com.uit.buddy.constant;

public final class MoodleApiConstants {

    private MoodleApiConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Function names
    public static final String FUNCTION_GET_SITE_INFO = "core_webservice_get_site_info";
    public static final String FUNCTION_GET_USERS_COURSES = "core_enrol_get_users_courses";
    public static final String FUNCTION_GET_COURSE_CONTENTS = "core_course_get_contents";
    public static final String FUNCTION_GET_ASSIGNMENT_SUBMISSIONS = "mod_assign_get_submission_status";

    // Parameter keys
    public static final String PARAM_WSTOKEN = "wstoken";
    public static final String PARAM_WSFUNCTION = "wsfunction";
    public static final String PARAM_MOODLEWSRESTFORMAT = "moodlewsrestformat";
    public static final String PARAM_USERID = "userid";
    public static final String PARAM_COURSEID = "courseid";
    public static final String PARAM_ASSIGNMENTID = "assignid";

    // Response Error Keys
    public static final String KEY_EXCEPTION = "exception";
    public static final String KEY_ERROR_CODE = "errorcode";

    public static final String ERROR_INVALID_TOKEN = "invalidtoken";
    public static final String ERROR_INVALID_LOGIN = "invalidlogin";
    public static final String ERROR_ACCESS_EXCEPTION = "accessexception";
    public static final String ERROR_NO_PERMISSION = "nopermission";
    public static final String ERROR_INVALID_PARAMETER = "invalidparameter";
    public static final String ERROR_INVALID_RECORD = "invalidrecord";

    //
}
