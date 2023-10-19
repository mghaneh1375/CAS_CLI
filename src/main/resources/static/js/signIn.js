var currElemId;

$(document).ready(function () {

    $.ajaxSetup({
        error : function(jqXHR, textStatus, errorThrown) {
            if (jqXHR.status === 400) {

                let errors = JSON.parse(jqXHR.responseJSON.msg.substr(jqXHR.responseJSON.msg.indexOf("{")));
                let errs = "";

                Object.keys(errors).forEach(key => {
                    errs += "<p>" + errors[key] + "</p>";
                });

                $("#" + currElemId).empty().append(errs);
            }
        }
    });

    $(window).keydown(function(event){
        if(event.keyCode === 13) {
            event.preventDefault();
            return false;
        }
    });

});

function phoneLogin() {
    var text = "";
    var html = "";
    text += '<form style="height: 100%;direction: rtl;">';
    text += '<div style="display:flex;flex-direction:column;gap:40px">';
    text +=
        '<div style="display:flex;justify-content: space-between;align-items: baseline;">';

    if(allowSignUp == "false")
        text +=
            '<div style="display:flex;align-items: center;color:white;padding-right: 10px;">ورود با تلفن همراه<div class="icon-mobile-login" style="font-size: 40px"></div></div>';
    else
        text +=
            '<div style="display:flex;align-items: center;color:white;padding-right: 10px;">ثبت نام/ ورود با تلفن همراه  <div class="icon-mobile-login" style="font-size: 40px"></div></div>';

    text +=
        '<div style="cursor:pointer;color:white;display: flex;align-self:flex-start;margin: 5px 0px 0px 10px;align-items: center;">';
    text += '<div onClick="location.reload()">بازگشت</div>';
    text += '<div class="icon-arrowleft" style="font-size: 20px"></div>';
    text += "</div>";
    text += "</div>";
    text +=
        '<div style="margin-right: 10px;box-shadow: 0px 3px 6px #00000025;width: fit-content;">';
    text +=
        '<input id="phoneNumInput" onkeypress="return isNumber(event)"  type="text" style="height:46px;width:250px;border: none;border-radius: 2px;padding-right: 10px;" placeholder="تلفن همراه را وارد کنید">';
    text += "";
    text += "</div>";
    text += '<div style="flex-direction: row-reverse;display:flex;">';
    text +=
        '<div id="nextStepEmail" onclick="sendSMS()" style="cursor:pointer;background-color:#FBC155;width:103px;height:46px;display: flex;align-items: center;justify-content: center;" >';
    text += "تایید</div>";
    text += "</div>";
    text += "<div>";
    text += '<div id="signUpErr" style="background-color:#FF0000">';
    text += "</div>";
    text += "</div>";
    text += "</form>";
    text += "";
    text += "";
    text += "</div>";
    $("#form").css("background-color", "#4DC7BC").empty().append(text);
    html += " (:(:(:(:(:(:(:سلام";
    $("#caption").empty().append(html);

    currElemId = "signUpErr";
}

function isNumber(evt) {
    evt = evt ? evt : window.event;
    var charCode = evt.which ? evt.which : evt.keyCode;
    return !(charCode > 31 && (charCode < 48 || charCode > 57));

}

var phone;
var token;
var code;
var timerElement;
var timerInterval;
var timeLimitInSeconds;

function sendSMS() {

    phone = $("#phoneNumInput").val();

    $.ajax({
        type: 'post',
        url: 'signUp',
        headers: {
            'content-type': 'application/json',
            'accept': 'application/json',
        },
        data: JSON.stringify({
            value: phone.toString(),
            via: 'SMS',
            callback: callback
        }),
        success: function (res) {
            if (res.status === 'ok') {
                token = res.token;
                timeLimitInSeconds = res.reminder;
                nextStepPhone();
            }
            else {
                $("#signUpErr").empty().append(res.msg);
            }
        }
    });

}

function nextStepPhone() {

    var text = "";
    var html = "";

    text += '<form style="height: 100%;direction: rtl;">';
    text += '<div style="display:flex;flex-direction:column;column;gap:20px">';
    text +=
        '<div style="display:flex;justify-content: space-between;align-items: baseline;">';
    text +=
        '<div style="display:flex;align-items: center;color:white;padding-right: 10px;">ثبت نام/ ورود با تلفن همراه <div class="icon-mobile-login" style="font-size: 40px"></div></div>';
    text +=
        '<div style="cursor:pointer; color:white;display: flex;align-self:flex-start;margin: 5px 0px 0px 10px;align-items: center;">';
    text += '<div onClick="phoneLogin()">بازگشت</div>';
    text += '<div class="icon-arrowleft" style="font-size: 20px"></div>';
    text += "</div>";
    text += "</div>";
    text += '<div class="boxInput">';
    text += '<div class="userInput" style="flex-direction: row-reverse  " >';

    text +=
        '<input type="text" onkeypress="return isNumber(event)" id="ist" maxlength="1" onkeyup="clickEvent(this,`sec`)">';
    text +=
        '<input type="text" onkeypress="return isNumber(event)" id="sec" maxlength="1" onkeyup="clickEvent(this,`third`)">';
    text +=
        '<input type="text" onkeypress="return isNumber(event)"  id="third" maxlength="1" onkeyup="clickEvent(this,`fourth`)">';
    text +=
        '<input type="text" onkeypress="return isNumber(event)" id="fourth" maxlength="1" onkeyup="clickEvent(this,`fifth`)">';
    text += "</div>";
    text += "</div>";
    text +=
        '<div style="display:flex;margin-right: 10px;flex-direction:column">';
    text +=
        '<div style="color:white;font-size:15px">لطفا کد اعتبار سنجی را وارد نمایید</div>';
    text +=
        '<div class="sendPhoneAgain" style="color:white;font-size:15px">کد چهار رقمی دوباره به تلفن همراهتان ارسال گردیده است.</div>';
    text += "";
    text += "</div>";
    text +=
        '<div style="flex-direction: row-reverse;display:flex;align-items: center;">';
    text +=
        '<button disabled onclick="stepTwoPhone()" style="color:white;font-size:12px;cursor:pointer;background-color:#720D19;width:103px;height:46px;display: flex;align-items: center;justify-content: center;flex-direction:column"  >';
    text += ' <div id="timer"></div><div> ارسال مجدد کد تا</div></button>';
    text +=
        '<div style="font-size:13px;padding-left:10px">آیا هنوز پیامک خود را دریافت نکردید؟';
    text += "</div>";
    text += "</div>";
    text += "<div>";
    text += '<div id="verificationCodeErr" style="background-color:#FF0000">';
    text += "</div>";
    text += "</div>";
    text += "</form>";
    text += "";
    text += "";
    text += "</div>";

    $("#form").css("background-color", "#4DC7BC").empty().append(text);

    html += " آیا شما خودتان هستید";
    $("#loginPic").attr("src", "../assets/loginVerification.svg").css("height", "80%");
    $("#caption").empty().append(html);
    timerElement = document.getElementById("timer");
    startCountDown();
}

function startTimer() {

    timeLimitInSeconds--;

    var minutes = Math.floor(timeLimitInSeconds / 60);
    var seconds = timeLimitInSeconds % 60;

    if (timeLimitInSeconds < 0) {
        timerElement.textContent = "00:00";
        clearInterval(timerInterval);
        return;
    }

    if (minutes < 10) {
        minutes = "0" + minutes;
    }
    if (seconds < 10) {
        seconds = "0" + seconds;
    }
    timerElement.textContent = minutes + ":" + seconds;

}

function startCountDown() {

    timerInterval = setInterval(startTimer, 1000);
    setInterval(function () {
        $("button").prop("disabled", false);
    }, timeLimitInSeconds);
}

function chooseUsername() {

    let username = $("#selectedUsername").val();

    $.ajax({
        type: 'post',
        url: 'setUsername',
        headers: {
            'content-type': 'application/json',
            'accept': 'application/json',
        },
        data: JSON.stringify({
            value: phone.toString(),
            token: token,
            code: code,
            username: username
        }),
        success: function (res) {

            if(res.status === 'ok') {
                $("#signInToken").val(token);
                $("#signInValue").val(phone);
                $("#signInCode").val(code);
                $("#signInForm").submit();
            }
            else {
                $("#chooseUsernameErr").empty().append(res.msg);
            }

        }
    });



}

function sendVerificationCode() {

    code = $("#ist").val() + $("#sec").val() + $("#third").val() + $("#fourth").val();

    if(code.length !== 4)
        return;

    $.ajax({
        type: 'post',
        url: 'checkCode',
        headers: {
            'content-type': 'application/json',
            'accept': 'application/json',
        },
        data: JSON.stringify({
            value: phone.toString(),
            token: token,
            code: code
        }),
        success: function (res) {

            if(res.status === 'ok') {
                if(!res.needUsername) {
                    $("#signInToken").val(token);
                    $("#signInValue").val(phone);
                    $("#signInCode").val(code);
                    $("#signInForm").submit();
                }
                else
                    stepTwoPhone();
            }
            else {
                $("#verificationCodeErr").empty().append(res.msg);
            }

        }
    });

}

function clickEvent(first, last) {
    if (first.value.length) {

        if(last === 'fifth')
            sendVerificationCode();

        document.getElementById(last).focus();
    }
}

function stepTwoPhone() {

    var text = "";
    var html = "";

    text += '<form style="height: 100%; direction: rtl;">';
    text += '<div style="display:flex;flex-direction:column;gap:20px">';
    text +=
        '<div style="display:flex;align-items: center;color:white;padding-right: 10px;">ثبت نام/ ورود با تلفن همراه  <div class="icon-mobile-login" style="font-size: 40px"></div></div>';
    text +=
        '<div style="margin-right: 10px;box-shadow: 0px 3px 6px #00000025;width: fit-content;">';
    text +=
        '<input id="selectedUsername" type="text" style="height:46px;width:250px;border: none;border-radius: 2px;padding-right: 10px;" placeholder="نام کاربری">';
    text += "</div>";
    text +=
        '<div style="display:flex;margin-right: 10px;flex-direction:column">';
    text +=
        '<div style="color:white;font-size:15px">دوستانتان شما را با این نام در سایت خواهند شناخت</div>';
    text += "</div>";
    text +=
        '<div style="flex-direction: row-reverse;display:flex;;justify-content: space-between;align-items: center;">';
    text +=
        '<div onclick="chooseUsername()" style="cursor:pointer;background-color:#FBC155;width:103px;height:46px;display: flex;align-items: center;justify-content: center;" >';
    text += "تایید</div>";
    text += '<div style="color:white;margin-right: 10px;">';
    text +=
        '<p style="font-size:10px">شما با ادامه ثبت نام در کوچیتا موافقت خود را با قوانین و مقررات آن اعلام نموده اید';
    text += "</p>";
    text +=
        '<p style="font-size:10px">همیشه می توانید از بخش <a style="color:#2439FC">قوانین و مقررات </a>در جریان محتوا و یا بروزرسانی های آن باشید. ';
    text += "</p>";
    text += "</div>";
    text += "</div>";
    text += "<div>";
    text += '<div id="chooseUsernameErr" style="background-color:#FF0000">';
    text += "</div>";
    text += "</div>";
    text += "</form>";
    text += "</div>";
    $("#form").css("background-color", "#4DC7BC").empty().append(text);
    html += " من کوچیتا هستم. شما؟";
    $("#loginPic").attr("src", "../assets/koochitaFace.svg").css("height", "80%");
    $("#caption").empty().append(html);
}