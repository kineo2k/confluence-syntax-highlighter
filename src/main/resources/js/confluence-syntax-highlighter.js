AJS.toInit(function(){
    document.querySelectorAll("span.sh-collapse").forEach(function(el) {
        el.addEventListener("click", function(ev) {
            if (el.dataset.collapse === "Y") {
                el.dataset.collapse = "N";
                el.querySelector(".expand-control-text").textContent = "코드 블록 접기";
                el.querySelector(".expand-control-icon").classList.add("expanded");
                el.parentElement.nextElementSibling.classList.remove("sh-hidden");
            } else {
                el.dataset.collapse = "Y";
                el.querySelector(".expand-control-text").textContent = "코드 블록 펼치기";
                el.querySelector(".expand-control-icon").classList.remove("expanded");
                el.parentElement.nextElementSibling.classList.add("sh-hidden");
            }
        });
    });
});
