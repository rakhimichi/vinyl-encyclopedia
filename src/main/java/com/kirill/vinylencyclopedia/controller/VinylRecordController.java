package com.kirill.vinylencyclopedia.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kirill.vinylencyclopedia.domain.CollectionSection;
import com.kirill.vinylencyclopedia.domain.VinylFormat;
import com.kirill.vinylencyclopedia.domain.VinylRecord;
import com.kirill.vinylencyclopedia.dto.VinylRecordFormDto;
import com.kirill.vinylencyclopedia.service.VinylRecordService;

import jakarta.validation.Valid;

@Controller
public class VinylRecordController {

    private final VinylRecordService vinylRecordService;

    public VinylRecordController(VinylRecordService vinylRecordService) {
        this.vinylRecordService = vinylRecordService;
    }

    @GetMapping("/records")
    public String showRecordsCatalog(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "sortBy", required = false, defaultValue = "artist") String sortBy,
            @RequestParam(name = "partial", required = false, defaultValue = "false") boolean partial,
            Authentication authentication,
            Model model
    ) {
        String username = authentication.getName();

        model.addAttribute("username", username);
        model.addAttribute("inMyCollectionRecords",
                vinylRecordService.getFilteredInMyCollectionRecordsForUser(username, query, sortBy));
        model.addAttribute("wishlistRecords",
                vinylRecordService.getFilteredWishlistRecordsForUser(username, query, sortBy));
        model.addAttribute("query", query == null ? "" : query);
        model.addAttribute("sortBy", sortBy);

        if (partial) {
            return "records :: catalogSections";
        }

        return "records";
    }

    @GetMapping("/records/new")
    public String showRecordEditor(
            @RequestParam(name = "section", required = false) CollectionSection section,
            Authentication authentication,
            Model model
    ) {
        String username = authentication.getName();

        VinylRecordFormDto recordForm = new VinylRecordFormDto();
        if (section == null) {
            section = CollectionSection.IN_MY_COLLECTION;
        }
        recordForm.setCollectionSection(section);

        populateRecordEditorModel(model, username, recordForm);
        return "record-editor";
    }

    @PostMapping("/records")
    public String createRecord(
            @Valid @ModelAttribute("recordForm") VinylRecordFormDto recordForm,
            BindingResult bindingResult,
            Authentication authentication,
            Model model
    ) {
        String username = authentication.getName();

        if (recordForm.getCollectionSection() == null) {
            recordForm.setCollectionSection(CollectionSection.IN_MY_COLLECTION);
        }

        if (bindingResult.hasErrors()) {
            populateRecordEditorModel(model, username, recordForm);
            return "record-editor";
        }

        vinylRecordService.createRecord(recordForm, username);
        return "redirect:/records/new?section=" + recordForm.getCollectionSection().name();
    }

    @GetMapping("/wishlist")
    public String redirectToWishlistSection() {
        return "redirect:/records#wishlist-section";
    }

    @GetMapping("/records/{id}")
    public String showRecordDetails(@PathVariable Long id, Authentication authentication, Model model) {
        String username = authentication.getName();
        VinylRecord record = vinylRecordService.getRecordForUser(id, username);

        model.addAttribute("record", record);
        model.addAttribute("username", username);

        return "record-details";
    }

    @GetMapping("/records/{id}/edit")
    public String showEditPage(@PathVariable Long id, Authentication authentication, Model model) {
        String username = authentication.getName();
        VinylRecord record = vinylRecordService.getRecordForUser(id, username);

        VinylRecordFormDto recordForm = new VinylRecordFormDto();
        recordForm.setArtist(record.getArtist());
        recordForm.setTitle(record.getTitle());
        recordForm.setGenre(record.getGenre());
        recordForm.setReleaseYear(record.getReleaseYear());
        recordForm.setLabelName(record.getLabelName());
        recordForm.setCollectionSection(record.getCollectionSection());
        recordForm.setVinylFormat(record.getVinylFormat());
        recordForm.setNotes(record.getNotes());
        recordForm.setCoverImagePath(record.getCoverImagePath());

        model.addAttribute("recordId", id);
        model.addAttribute("recordForm", recordForm);
        model.addAttribute("collectionSections", CollectionSection.values());
        model.addAttribute("vinylFormats", VinylFormat.values());

        return "edit-record";
    }

    @PostMapping("/records/{id}/edit")
    public String updateRecord(
            @PathVariable Long id,
            @Valid @ModelAttribute("recordForm") VinylRecordFormDto recordForm,
            BindingResult bindingResult,
            Authentication authentication,
            Model model
    ) {
        String username = authentication.getName();

        if (bindingResult.hasErrors()) {
            model.addAttribute("recordId", id);
            model.addAttribute("collectionSections", CollectionSection.values());
            model.addAttribute("vinylFormats", VinylFormat.values());
            return "edit-record";
        }

        vinylRecordService.updateRecord(id, recordForm, username);
        return "redirect:/records/" + id;
    }

    @PostMapping("/records/{id}/delete")
    public String deleteRecord(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String username = authentication.getName();
        vinylRecordService.deleteRecord(id, username);
        return "redirect:/records";
    }

    private void populateRecordEditorModel(Model model, String username, VinylRecordFormDto recordForm) {
        CollectionSection currentSection = recordForm.getCollectionSection() != null
                ? recordForm.getCollectionSection()
                : CollectionSection.IN_MY_COLLECTION;

        String editorHeading = currentSection == CollectionSection.WISHLIST
                ? "Add to Wishlist"
                : "Add to My Collection";

        String editorDescription = currentSection == CollectionSection.WISHLIST
                ? "Add a record that you want to buy later. You can still preview your current catalog on the right."
                : "Add a vinyl record to My Collection and immediately see it appear in the preview list.";

        String submitLabel = currentSection == CollectionSection.WISHLIST
                ? "Add to Wishlist"
                : "Add to My Collection";

        String alternateActionLabel = currentSection == CollectionSection.WISHLIST
                ? "Add to My Collection"
                : "Add to Wishlist";

        String alternateActionLink = currentSection == CollectionSection.WISHLIST
                ? "/records/new?section=IN_MY_COLLECTION"
                : "/records/new?section=WISHLIST";

        model.addAttribute("recordForm", recordForm);
        model.addAttribute("username", username);
        model.addAttribute("records", vinylRecordService.getRecordsForUser(username));
        model.addAttribute("collectionSections", CollectionSection.values());
        model.addAttribute("vinylFormats", VinylFormat.values());
        model.addAttribute("editorHeading", editorHeading);
        model.addAttribute("editorDescription", editorDescription);
        model.addAttribute("submitLabel", submitLabel);
        model.addAttribute("alternateActionLabel", alternateActionLabel);
        model.addAttribute("alternateActionLink", alternateActionLink);
    }
}
