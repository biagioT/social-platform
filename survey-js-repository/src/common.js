import $ from 'jquery';
// qui il codice per webpack

export const pushSurvey = function(name, data, success){
 
    var jsonData = {
        name: name,
        data: data
    }

    $.ajax({
      type: "POST",
      url: "http://localhost:8080/push-survey",
      data: JSON.stringify(jsonData),
      contentType:"application/json; charset=utf-8",
      dataType:"json",
      success: success
      
    });
}

