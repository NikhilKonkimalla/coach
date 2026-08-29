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

// A checkbox with data-disables="someFieldId" clears and disables (and
// un-requires) that field while checked — e.g. "lifelong goal" vs. a
// required target date.
document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll("input[type=checkbox][data-disables]").forEach(function (checkbox) {
        var target = document.getElementById(checkbox.dataset.disables);
        if (!target) return;

        var sync = function () {
            target.disabled = checkbox.checked;
            target.required = !checkbox.checked && target.dataset.requiredWhenEnabled !== "false";
            if (checkbox.checked) target.value = "";
        };
        checkbox.addEventListener("change", sync);
        sync();
    });
});

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
