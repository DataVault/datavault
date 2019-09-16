// This is a nasty hack necessitated by the force logout functionality when the user's permissions change. When the user
// is force logged out, if their next request is an AJAX request then they'll get the login page markup back in a
// HTTP 200 as a string instead of the data they were expecting - in which case just redirect to the login page instead
// of trying to process the page markup as if it were the expected data.
const ErrorHandler = {
    initialiseSpringContextPath: function(springContextPath) {
        ErrorHandler._springContextPath = springContextPath;
    },
    isForceLogoutResponse: function(data) {
        return typeof data === 'string' && data.length > 0;
    },
    handleForceLogoutResponse: function() {
        window.location.href = ErrorHandler._springContextPath + '/auth/login?security';
    },
    handleAjaxError: function(errorSelector, xhr) {
        if (xhr.status === 422) {
            $(errorSelector).removeClass('hidden').html(xhr.responseText.replace("\n", "<br/>"));
        } else {
            document.open();
            document.write(xhr.responseText);
            document.close();
        }
    }
};
