jQuery(document).ready(function(){
    jQuery('select[name=cascadingProjectName]').change(function() {
        var url = window.location.href;
        var jobUrl = url.substr(0, url.lastIndexOf('/'))+'/updateCascadingProject';
        var cascadingProject = jQuery(this).val();
        new Ajax.Request(jobUrl+'?projectName='+cascadingProject, {
            method : 'get',
            onSuccess : function(x) {
                location.reload(true);
            },
        });
   });
});