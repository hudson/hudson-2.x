function onLinkClick(dialogContainerId) {
    jQuery.blockUI({
      message: jQuery('#' + dialogContainerId),
      css: {
        width: '350px',
        border: '2px solid #FFFFFF',
        padding: '5px',
        backgroundColor: '#000',
        '-webkit-border-radius': '10px',
        '-moz-border-radius': '10px',
        opacity: .6,
        color: '#fff'
      },
      title:  'Confirmation',
    });
}

jQuery(document).ready(function() {
    jQuery('.yes').click(function() {
        setTimeout(jQuery.unblockUI, 4000);
    });
    jQuery('.no').click(function() {
        jQuery.unblockUI();
        return false;
    });
});
