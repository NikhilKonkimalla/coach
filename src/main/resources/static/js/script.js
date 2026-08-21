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

// Forms marked class="form-loading" show a spinner on their submit button
// while the page navigates to the next (possibly slow) response.
document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll("form.form-loading").forEach(function (form) {
        form.addEventListener("submit", function () {
            if (form.dataset.submitted === "true") return;
            form.dataset.submitted = "true";

            var button = form.querySelector('button[type="submit"]');
            if (!button) return;

            var loadingText = button.dataset.loadingText || "Working…";
            button.dataset.originalHtml = button.innerHTML;
            button.disabled = true;
            button.classList.add("is-loading");
            button.innerHTML = '<span class="btn-spinner"></span>' + loadingText;
        });
    });
});
