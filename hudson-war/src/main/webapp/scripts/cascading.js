/*
 * The MIT License
 *
 * Copyright (c) 2011, Oracle Corporation, Inc., Nikita Levyankov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

hudsonRules["A.reset-button"] = function(e) {
    e.onclick = function() {
        new Ajax.Request(this.getAttribute("resetURL"), {
                method : 'get',
                onSuccess : function(x) {
                    location.reload(true);
                },
                onFailure : function(x) {

                }
            });
        return false;
    }
    e.tabIndex = 9999; // make help link unnavigable from keyboard
    e = null; // avoid memory leak
}

function getJobUrl() {
    var url = window.location.href;
    return url.substr(0, url.lastIndexOf('/'))
}

function onCascadingProjectUpdated() {
    if(isRunAsTest) return;
    jQuery('select[name=cascadingProjectName]').change(function() {
        var jobUrl = getJobUrl()+'/updateCascadingProject';
        var cascadingProject = jQuery(this).val();
        new Ajax.Request(jobUrl+'?projectName='+cascadingProject, {
            method : 'get',
            onSuccess : function(x) {
                location.reload(true);
            }
        });
    });
}

function onProjectPropertyChanged() {
    if(isRunAsTest) return;
    var modify = function() {
        var ref = jQuery(this).attr('id');
        var cascadingProperty = '';
        if (ref != '') {
            cascadingProperty = jQuery(this).attr('name');
        } else {
            var parent = jQuery(this).parents('tr');
            while (parent.attr("nameref") == undefined && parent.size() !== 0) {
                parent = jQuery(parent).parents('tr');
            }
            var childRef = parent.attr("nameref");
            cascadingProperty = jQuery('#'+childRef).attr('name');
        }
        if(cascadingProperty !== undefined) {
            var jobUrl = getJobUrl()+'/modifyCascadingProperty?propertyName='+cascadingProperty;
            new Ajax.Request(jobUrl, {
                method : 'get'
            });
        }
    };
    jQuery("form[action=configSubmit] input[type=checkbox]").live('click', modify);
    jQuery("form[action=configSubmit] input[type!=checkbox]").live('change', modify);    
    jQuery("form[action=configSubmit] .setting-input").live('change', modify);
    jQuery("form[action=configSubmit] button").live('click', modify);
}

jQuery(document).ready(function(){
    onCascadingProjectUpdated();
    onProjectPropertyChanged();
});
