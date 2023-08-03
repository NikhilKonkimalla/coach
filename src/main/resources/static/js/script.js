function showAdditionalInfo(checkbox) {
    var checkboxId = checkbox.id;
    var additionalInfoId = "additionalInfo" + checkboxId.substr(checkboxId.length - 1);
    var additionalInfo = document.getElementById(additionalInfoId);

    if (checkbox.checked) {
        additionalInfo.style.display = "block";
    } else {
        additionalInfo.style.display = "none";
    }
}
