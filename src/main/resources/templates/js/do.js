$('#bRequest').submit(function(e) {

    var requestStr = $("input[id='bword']").value();
    console.log("requestStr :" +requestStr);
    if(requestStr !== null) {
        getFilteredResult(requestStr);
    }

    else {
        console.log(e);
        alert("내용이 비어있습니다");
    }
});



