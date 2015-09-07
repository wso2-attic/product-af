/*
 *
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */
function compareObject(obj1, obj2, property) {
    return (obj1[property] < obj2[property]) ? -1 : (obj1[property] > obj2[property]) ? 1 : 0;
}

/**
 * compare Function to object arrays based on the {@code property}. If you want to sort in descending manner,
 * start the property name with "-"(ex: unsortedArray.sort(dynamicSort("-first_name"));)
 *
 * usage: unsortedArray.sort(dynamicSort("first_name"));
 *
 * ex :
 *  var unsortedArray =  [
 *      { first_name: 'Ben',    last_name: 'Smith'},
 *      { first_name: 'Julian', last_name: 'Savea'},
 *      { first_name: 'Aaron', last_name: 'Smith'}
 * ];
 *
 * result of unsortedArray.sort(dynamicSort("first_name")); would be
 * [
 *      { first_name: 'Aaron', last_name: 'Smith'},
 *      { first_name: 'Ben',    last_name: 'Smith'},
 *      { first_name: 'Julian', last_name: 'Savea'},
 * ];
 *
 * result of unsortedArray.sort(dynamicSort("-first_name")); would be
 * [
 *      { first_name: 'Julian', last_name: 'Savea'},
 *      { first_name: 'Ben',    last_name: 'Smith'},
 *      { first_name: 'Aaron', last_name: 'Smith'},
 * ];
 *
 * @param property(key) to be considered when comparing the objects of array. If you want to sort in descending manner,
 * start the property name with "-"
 * @returns {Function}
 */
function dynamicSort(property) {
    var sortOrder = 1;
    if (property[0] === "-") {
        sortOrder = -1;
        property = property.substr(1);
    }
    return function (array1, array2) {
        var result = compareObject(array1, array2, property);
        return result * sortOrder;
    }
}

/**
 * compare Function to object arrays based on the {@code propertiesArray}. If you want to sort in descending manner,
 * start the property name with "-"(ex: unsortedArray.sort(dynamicSort("-first_name" , "last_name"));)
 *
 * usage: unsortedArray.sort(dynamicSort("first_name"));
 *
 * ex :
 *  var unsortedArray =  [
 *      { first_name: 'Ben',    last_name: 'Smith'},
 *      { first_name: 'Julian', last_name: 'Savea'},
 *      { first_name: 'Ben', last_name: 'Collins'}
 * ];
 *
 * result of unsortedArray.sort(dynamicSort("-first_name", "last_name")); would be
 * [
 *      { first_name: 'Julian', last_name: 'Savea'},
 *      { first_name: 'Ben',    last_name: 'Collins'},
 *      { first_name: 'Ben', last_name: 'Smith'},
 * ];
 *
 * @param propertiesArray to be considered when comparing the objects of array. If you want to sort in descending manner,
 * start the property name with "-"
 * @returns {Function}
 */
function dynamicSortMultiple(propertiesArray) {
    return function (array1, array2) {
        var i = 0, result = 0, numberOfProperties = propertiesArray.length;
        /* try getting a different result from 0 (equal)
         * as long as we have extra properties to compare
         */
        while (result === 0 && i < numberOfProperties) {
            result = dynamicSort(propertiesArray[i])(array1, array2);
            i++;
        }
        return result;
    }
}

/**
 * Finds the intersection of two basic arrays
 * @param sortedArrayA - first array, must already be sorted
 * @param sortedArrayB - second array, must already be sorted
 * @returns {Array} intersection of the arrays
 */
function intersection(sortedArrayA, sortedArrayB) {
    var ai = 0, bi = 0;
    var result = [];

    while (ai < sortedArrayA.length && bi < sortedArrayB.length) {
        if (sortedArrayA[ai] < sortedArrayB[bi]) {
            ai++;
        }
        else if (sortedArrayA[ai] > sortedArrayB[bi]) {
            bi++;
        }
        else /* they're equal */
        {
            result.push(sortedArrayA[ai]);
            ai++;
            bi++;
        }
    }

    return result;
}


/**
 * Finds the intersection of two "Object" arrays.
 *
 * Note that it will add the matching objects of the {@code sortedArrayA} to the resulting intersection array
 * ex:
 * var sortedArrayA =  [
 *      { first_name: 'Aaron', last_name: 'Smith'},
 *      { first_name: 'Ben',    last_name: 'Smith'},
 *      { first_name: 'Julian', last_name: 'Savea'},
 * ];
 *
 * var sortedArrayB =  [
 *      { first_name: 'Ben',    last_name: 'Collins'},
 *      { first_name: 'Daniel', last_name: 'Carter' },
 *      { first_name: 'Julian', last_name: 'Savea'},
 * ];
 *
 * result of the intersect_safe(sortedArrayA, sortedArrayB, "first_name") would be
 * [
 *      { first_name: 'Ben',    last_name: 'Smith'}, // note that this entry is picked from the first array(sortedArrayA)
 *      { first_name: 'Julian', last_name: 'Savea'},
 * ];
 *
 * @param sortedArrayA - first array, must already be sorted
 * @param sortedArrayB - second array, must already be sorted
 * @param property  - property of the to be considered to check the equality of the two objects
 * @returns {Array} intersection of the Object arrays
 *
 */
function intersection(sortedArrayA, sortedArrayB, property) {
    var ai = 0, bi = 0, comparedResult = 0;
    var result = [];

    while (ai < sortedArrayA.length && bi < sortedArrayB.length) {
        comparedResult = compareObject(sortedArrayA[ai], sortedArrayB[bi], property);
        if (comparedResult < 0) {
            ai++;
        } else if (comparedResult > 0) {
            bi++;
        } else { // they're equal
            result.push(sortedArrayA[ai]);
            ai++;
            bi++;
        }
    }

    return result;
}

/**
 * Finds the inverse intersection of two basic arrays
 * @param sortedArrayA - first array, must already be sorted
 * @param sortedArrayB - second array, must already be sorted
 * @returns {Array} intersection of the arrays
 */
function inverseIntersection(sortedArrayA, sortedArrayB) {
    var ai = 0, bi = 0;
    var result = [];

    while (ai < sortedArrayA.length && bi < sortedArrayB.length) {
        if (sortedArrayA[ai] < sortedArrayB[bi]) {
            result.push(sortedArrayA[ai]);
            ai++;
        }
        else if (sortedArrayA[ai] > sortedArrayB[bi]) {
            result.push(sortedArrayB[bi]);
            bi++;
        }
        else // they're equal
        {
            ai++;
            bi++;
        }
    }

    // concatenating remaining unmatched elements
    if (ai < sortedArrayA.length) {
        result = result.concat(sortedArrayA.slice(ai));
    }
    if (bi < sortedArrayB.length) {
        result = result.concat(sortedArrayB.slice(bi));
    }

    return result;
}


/**
 * Finds the inverse intersection of two "Object" arrays.
 * usage: inverseIntersection(sortedArrayA, sortedArrayB, "first_name")
 *
 * ex:
 * var sortedArrayA =  [
 *      { first_name: 'Aaron', last_name: 'Smith'},
 *      { first_name: 'Ben',    last_name: 'Smith'},
 *      { first_name: 'Julian', last_name: 'Savea'},
 * ];
 *
 * var sortedArrayB =  [
 *      { first_name: 'Ben',    last_name: 'Collins'},
 *      { first_name: 'Daniel', last_name: 'Carter' },
 *      { first_name: 'Julian', last_name: 'Savea'},
 * ];
 *
 * result of the inverseIntersection(sortedArrayA, sortedArrayB, "first_name") would be
 * [
 *      { first_name: 'Aaron', last_name: 'Smith'},
 *      { first_name: 'Daniel', last_name: 'Carter' },
 * ];
 *
 * @param sortedArrayA - first array, must already be sorted
 * @param sortedArrayB - second array, must already be sorted
 * @param property  - property of the to be considered to check the equality of the two objects
 * @returns {Array} intersection of the Object arrays
 *
 */
function inverseIntersection(sortedArrayA, sortedArrayB, property) {
    var ai = 0, bi = 0, comparedResult = 0;
    var result = [];

    while (ai < sortedArrayA.length && bi < sortedArrayB.length) {
        comparedResult = compareObject(sortedArrayA[ai], sortedArrayB[bi], property);
        if (comparedResult < 0) {
            result.push(sortedArrayA[ai]);
            ai++;
        } else if (comparedResult > 0) {
            result.push(sortedArrayB[bi]);
            bi++;
        } else { // they're equal
            ai++;
            bi++;
        }
    }

    // concatenating remaining unmatched elements
    if (ai < sortedArrayA.length) {
        result = result.concat(sortedArrayA.slice(ai));
    }
    if (bi < sortedArrayB.length) {
        result = result.concat(sortedArrayB.slice(bi));
    }

    return result;
}

