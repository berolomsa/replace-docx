jQuery(document).ready(function(){
      $.ajax({
        dataType: "json",
        url: "http://localhost:8080/all",
        success: function(response) {
			var html = '';
			$.each(response,function(i, item) {
				html += '<tr class="d-flex"><td class="col-8">' + item+ '</td> \
                                   <td class="col-2">\
                                       <button alt="'+item+'" type="button" class="btn btn-primary download">\
                                           <span class="fas fa-arrow-circle-down"></span>\
                                       </button>\
                                   </td>\
                                   <td class="col-2">\
                                       <button alt="'+item+'" type="button" class="btn btn-danger delete">\
                                           <span class="fas fa-trash-alt"></span>\
                                       </button>\
                                   </td>\
                               </tr>'
			});
			$('#table-body').append(html);
			$(".delete").click(function(){
				  var url = 'http://localhost:8080/deleteFile/templates/' + $(this).attr('alt');
							$.ajax({
								  type:'DELETE',
								  url: url,
								  success: function(response) {
									location.reload();
								  }
								});
			});
			$(".download").click(function(){
				  var url = 'http://localhost:8080/files/templates/' + $(this).attr('alt');
				  window.open(url);

			});
        },
      });

	  $("form").submit(function(evt){
            evt.preventDefault();
            var formData = new FormData();
            formData.append('file', $('.file-input')[0].files[0])
         	var xhr = new XMLHttpRequest();
			xhr.open('POST', 'http://localhost:8080/uploadFile', true);
			xhr.onload = function () {
			  if (xhr.status === 200) {
				location.reload();
			  } else {
				alert('An error occurred!');
			  }
			};
			xhr.send(formData);
         return false;
       });

		$("[type=file]").on("change", function(){
		  var file = this.files[0].name;
		  var dflt = $('#files-label');
		  if($(this).val()!=""){
			$(this).next().text(file);
		  } else {
			$(this).next().text(dflt);
		  }
		});
});