/**
 * This method validates the database result, whether the result contains any null values
 *
 * @param obj - a row entry, returned from database query
 * @param labelArray - array of attributes, which need to be check
 * @returns {boolean} - true if the query result is valid, false if the query result is not valid
 */
function isValidDbResult(obj, labelArray) {
    if (obj && labelArray) {
        for (var i = 0; i < labelArray.length; i++) {
            if (!obj[labelArray[i]]) {
                return false;
            }
        }
        return true;
    }
    return false;
}