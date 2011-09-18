/**
 * 
 */

jQuery(document).ready(function() {
	jQuery( ".whats-new-main" ).tabs();
	jQuery(".whats-new-view-group-info-preview-link").fancybox({
		width: windowinfo.getWindowWidth() * 0.5
		,height: windowinfo.getWindowHeight() * 0.7
		,autoDimensions: false
	});
});