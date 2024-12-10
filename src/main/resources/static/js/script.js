//完了申請ボタンのアラート
function CheckRequest(){
    let result = window.confirm("申請してもよろしいですか？");
    if (!result) return false;
}

//承認ボタンのアラート
function CheckApprove(){
    let result = window.confirm("承認してもよろしいですか？");
    if (!result) return false;
}

//差し戻しボタンのアラート
function CheckSendBack(){
    let result = window.confirm("差し戻ししてもよろしいですか？");
    if (!result) return false;
}

//削除ボタンのアラート
function CheckDelete(){
    let result = window.confirm("削除してもよろしいですか？");
    if (!result) return false;
}