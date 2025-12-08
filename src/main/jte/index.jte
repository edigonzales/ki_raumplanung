@param java.util.List<ch.so.arp.rag.chat.DocumentResult> results
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=0" />
    <meta name="mobile-web-app-capable" content="yes" />
    
    <title>KI Raumplanung</title>
    
    <link rel="stylesheet" href="fonts.css">    
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">    
    
    <script src="https://unpkg.com/htmx.org@2.0.8"></script>
    <script src="https://cdn.jsdelivr.net/npm/htmx-ext-sse@2.2.4/sse.min.js"></script>
    <style>
        html {
            font-size: 14px;
        }
        
        body,
        button, input, optgroup, select, textarea {
            font-family: Frutiger, system-ui, Roboto, sans;txdfgdfgdfgdfgdfgdfgdfg
            color: #333333;
            font-size: 1rem;
        }
        
        .layout { 
            padding-left: 1rem;
            display: flex; 
            gap: 2rem; 
            align-items: 
            flex-start; 
        }
        
        .column { flex: 1; }
     
       .input-container {
          position: relative;
          width: 80%; 
          margin-right: 20px;
        }     
       
.form-group {
    margin-bottom: 1rem;
}       
        
.form-label {
    display: block;
    font-size: 0.95rem;
    font-weight: 400;          /* not too bold */
    color: #555555;            /* slightly lighter than #333 */
    margin-bottom: 0.25rem;    /* little gap to select/input/button */
}        
       .search-container {
          position: relative;
        }     

        .search-input {
            width: 100%;
            height: 52px;
            background-color: #F4F4F4;
            border-top: 0px solid black;
            border-right: 0px solid black;
            border-bottom: 1px solid black;
            border-left: 0px solid black;
            box-sizing: border-box;
            padding: 0 5px 0 30px; /* Added left padding for the icon */
            outline: none; /* Remove default blue/orange outline */
            font-size: 16px;
            color: #333;
        }        
        
.select-input {
    width: 100%;
    height: 52px;
    background-color: #F4F4F4;
    border-top: 0;
    border-right: 0;
    border-bottom: 1px solid #000;
    border-left: 0;
    box-sizing: border-box;
    padding: 0 12px;
    font-size: 16px;
    color: #333;
    outline: none;
}

.btn-input {
    height: 52px;
    background-color: #F4F4F4;
    border-top: 0;
    border-right: 0;
    border-left: 0;
    border: 1px solid rgb(198, 40, 40); 
    border-radius: 3px;   
    box-sizing: border-box;
    padding: 0 12px;
    font-size: 16px;
    color: rgb(198, 40, 40);
    outline: none;
}

        .search-input:focus, 
        .select-input:focus {
            border-bottom: 1px solid rgb(198, 40, 40);
            outline: none;
        }
               
.magnifier-icon,
.clear-icon {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);  /* true vertical centering */
  display: flex;
  align-items: center;
  font-size: 16px;
  color: #666;
  z-index: 1;
}
             
/* left icon */
.magnifier-icon {
  left: 5px;
  pointer-events: none;
}

/* right icon */
.clear-icon {
  right: 5px;
  cursor: pointer;
}
               

.select-input {
    /* remove rounded corners / native look differences */
    border-radius: 0;
    
    /* keep default arrow but tidy up spacing */
    padding-right: 32px;
    
    /* if you want to suppress OS-specific styling, uncomment:
    -webkit-appearance: none;
    -moz-appearance: none;
    appearance: none;
    */
}

/* base slider */
input[type="range"] {
    -webkit-appearance: none;
    -moz-appearance: none;
    appearance: none;
    width: 100%;
    background: transparent;
    outline: none;

    /* for WebKit: grey + red gradient, split at --range-progress */
    height: 6px;
    border-radius: 3px;
    background: linear-gradient(
        to right,
        rgb(198, 40, 40) 0 var(--range-progress, 0%),
        #e4e4e4          var(--range-progress, 0%) 100%
    );
}

/* WebKit track must be transparent so gradient above shows */
input[type="range"]::-webkit-slider-runnable-track {
    height: 6px;
    border-radius: 3px;
    background: transparent;
    border: none;
}

/* WebKit thumb */
input[type="range"]::-webkit-slider-thumb {
    -webkit-appearance: none;
    height: 20px;
    width: 20px;
    border-radius: 50%;
    background: rgb(198, 40, 40);
    border: none;
    box-shadow: none;
    margin-top: -7px; /* center on 6px track */
}

/* Firefox track + progress */
input[type="range"]::-moz-range-track {
    height: 6px;
    border-radius: 3px;
    background: #e4e4e4;      /* grey part */
    border: none;
}

input[type="range"]::-moz-range-progress {
    height: 6px;
    border-radius: 3px;
    background: rgb(198, 40, 40);  /* red filled part */
}

/* Firefox thumb */
input[type="range"]::-moz-range-thumb {
    height: 20px;
    width: 20px;
    border-radius: 50%;
    background: rgb(198, 40, 40);
    border: none;
    box-shadow: none;
}

/* (optional) IE/old Edge – same idea as before */
input[type="range"]::-ms-track {
    height: 6px;
    background: transparent;
    border-color: transparent;
    color: transparent;
}
input[type="range"]::-ms-fill-lower {
    background: rgb(198, 40, 40);
}
input[type="range"]::-ms-fill-upper {
    background: #e4e4e4;
}
input[type="range"]::-ms-thumb {
    height: 20px;
    width: 20px;
    border-radius: 50%;
    background: rgb(198, 40, 40);
    border: none;
}

.btn-input {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    text-align: center;
    white-space: nowrap;
}

/* simple hover / active states */
.btn-input:hover {
    background-color: #e9e9e9;
}

.btn-input:active {
    background-color: #dedede;
}
    
    
       .task-container {
          position: relative;
          width: 80%; 
          margin-right: 20px;
        }     
    
.textarea-input {
    width: 100%;
    min-height: 120px;              /* adjustable – choose your preferred height */
    background-color: #F4F4F4;
    
    border-top: 0;
    border-right: 0;
    border-bottom: 1px solid #000;
    border-left: 0;

    padding: 12px;
    box-sizing: border-box;

    font-size: 16px;
    font-family: Frutiger, system-ui, Roboto, sans;
    color: #333;

    resize: vertical;               /* allow resizing vertically, optional */
    outline: none;
}

.textarea-input:focus {
    border-bottom: 1px solid rgb(198, 40, 40);  /* red highlight */
    outline: none;
}    
    
/* Wrapper for checkbox + label text */
.checkbox-group {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    cursor: pointer;
    font-size: 1rem;
    color: #333;
}

/* Hide native checkbox */
.checkbox-group input[type="checkbox"] {
    position: absolute;
    opacity: 0;
    pointer-events: none;
}

/* Custom box */
.checkbox-custom {
    width: 20px;
    height: 20px;
    box-sizing: border-box;
    background-color: #F4F4F4;   /* same grey as inputs */
    border: 2px solid #666;
    border-radius: 3px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    transition: background-color 0.15s ease, border-color 0.15s ease;
}

/* Checkmark */
.checkbox-custom::after {
    content: "";
    width: 10px;
    height: 6px;
    border-left: 2px solid #fff;
    border-bottom: 2px solid #fff;
    transform: rotate(-45deg) scale(0);
    transform-origin: center;
    transition: transform 0.15s ease;
}

/* Checked state: red fill */
.checkbox-group input[type="checkbox"]:checked + .checkbox-custom {
    background-color: rgb(198, 40, 40);
    border-color: rgb(198, 40, 40);
}

/* Show checkmark when checked */
.checkbox-group input[type="checkbox"]:checked + .checkbox-custom::after {
    transform: rotate(-45deg) scale(1);
}

/* Focus outline for accessibility */
.checkbox-group input[type="checkbox"]:focus + .checkbox-custom {
    outline: 1px solid rgb(198, 40, 40);
    outline-offset: 2px;
}

/* Generic disabled state */
input:disabled,
select:disabled,
textarea:disabled,
button:disabled {
    color: #9e9e9e;          /* greyed-out text */
    background-color: #F4F4F4;
    border-color: #cccccc;   /* optional: soften the bottom border */
    cursor: not-allowed;
}

/* If you want to target only your styled controls: */
.search-input:disabled,
.select-input:disabled,
.textarea-input:disabled,
.btn-input:disabled {
    color: #9e9e9e;
    background-color: #F4F4F4;
    border-bottom-color: #cccccc;
}

/* Checkbox label text when disabled */
.checkbox-group input[type="checkbox"]:disabled ~ span {
    color: #9e9e9e;
    cursor: not-allowed;
}

/* Optional: dim the custom checkbox box itself */
.checkbox-group input[type="checkbox"]:disabled + .checkbox-custom {
    border-color: #cccccc;
    background-color: #f0f0f0;
}
    
    
        .result-card { border: 1px solid #000; padding: 0.5rem; margin-bottom: 0.5rem; }
        .document-header { display: flex; justify-content: space-between; gap: 0.5rem; align-items: flex-start; }
        .section-card { border: 1px solid #ddd; padding: 0.5rem; margin-top: 0.5rem; background: #fafafa; }
        .section-card details { margin-top: 0.25rem; }
        .section-card summary { cursor: pointer; } Stefan Ziegelr
        .full-document-toggle { display: flex; justify-content: flex-start; margin: 0.5rem 0; }
        .full-document-toggle label { font-weight: 400; }
        .task-controls { display: flex; flex-direction: column; align-items: flex-start; gap: 0.5rem; margin: 0.5rem 0; }
        .task-select { display: flex; flex-direction: column; text-align: left; }
    </style>
</head>
<body>
<div class="container">
    <div style="margin-top: 10px; text-align: right;">
        <img src="Logo.png" alt="Logo Kanton Solothurn">
    </div>
</div>
<h1 style="margin-top: 0px;">KI Raumplanung</h1>
<div class="layout">
    <div class="column">
        <h2>Schritt 1: Dokumente suchen</h2>
        <form hx-post="/search" hx-target="#search-results" hx-swap="innerHTML">
        
            <div class="input-container">
            
                <div class="form-group">
                    <label for="keywords" class="form-label">Stichworte</label>            
                    <div class="search-container">
                        <span class="magnifier-icon"><i class="bi bi-search" style="color: #333; font-size:1.2rem;"></i></span> 
                        <input id="keywords" class="search-input"
                            type="text" 
                            name="keywords" 
                            autocomplete="off" 
                            placeholder="Stichworte" autofocus required/>
                            <span class="clear-icon" hx-on:click="document.querySelector('#keywords').value = ''; htmx.trigger('#keywords', 'input');"><i class="bi bi-x" style="color: #333; font-size:1.2rem;"></i></span>
                    </div>
                </div>

                <div class="form-group">            
                    <label for="gemeinde" class="form-label">Gemeinde</label>
                    <select id="gemeinde" class="select-input" name="municipality">
                        <option value=""></option>
                        <option value="Balsthal">Balsthal</option>
                        <option value="Bellach">Bellach</option>
                        <option value="Biberist">Biberist</option>
                        <option value="Deitingen">Deitingen</option>
                        <option value="Dornach">Dornach</option>
                        <option value="Dulliken">Dulliken</option>
                        <option value="Erlinsbach">Erlinsbach</option>
                        <option value="Gerlafingen">Gerlafingen</option>
                        <option value="Gretzenbach">Gretzenbach</option>
                        <option value="Härkingen">Härkingen</option>
                        <option value="Himmelried">Himmelried</option>
                        <option value="Kappel">Kappel</option>
                        <option value="Langendorf">Langendorf</option>
                        <option value="Lüsslingen-Nennigkofen">Lüsslingen-Nennigkofen</option>
                        <option value="Niedergösgen">Niedergösgen</option>
                        <option value="Oberbuchsiten">Oberbuchsiten</option>
                        <option value="Oensingen">Oensingen</option>
                        <option value="Olten">Olten</option>
                        <option value="Recherswil">Recherswil</option>
                        <option value="Riedholz">Riedholz</option>
                        <option value="Rohr">Rohr</option>
                        <option value="Schönenwerd">Schönenwerd</option>
                        <option value="Selzach">Selzach</option>
                        <option value="Solothurn">Solothurn</option>
                        <option value="Starrkirch-Wil">Starrkirch-Wil</option>
                        <option value="Stüsslingen">Stüsslingen</option>
                        <option value="Trimbach">Trimbach</option>
                        <option value="Zuchwil">Zuchwil</option>
                    </select>
                </div>
                                
                <div class="form-group">
                    <label for="planungsinstrument" class="form-label">Planungsinstrument</label>
                    <select id="planungsinstrument" class="select-input" name="plan_type">
                        <option value=""></option>
                        <option value="gestaltungsplan">gestaltungsplan</option>
                        <option value="ortsplanung">ortsplanung</option>
                    </select>
                </div> 
            
                <div class="form-group">            
                    <label for="lexicalWeight" class="form-label">Lexikalisches Gewicht (0 = semantisch, 1 = lexikalisch):</label>
                    <input type="range" id="lexicalWeight" name="lexicalWeight" min="0" max="1" step="0.05" value="0.6">
                    <span id="lexicalWeightValue">0.60</span>
                </div>
            </div>            
            
            <button type="submit" class="btn-input">Suchen</button>
        </form>
        <div id="search-results">
            <p>Keine Suche ausgeführt.</p>
        </div>
    </div>
    <div class="column">
        <h2>Schritt 2: Aufgabe stellen</h2>
        <form hx-post="/task" hx-target="#task-panel" hx-swap="innerHTML"> 
        
            <div class="task-container">
                <div class="form-group">                        
                    <label for="prompt" class="form-label">Aufgabe</label>
                    <textarea id="prompt" class="textarea-input" name="prompt" rows="4" cols="50" required>Fasse die ausgewählten Abschnitte zum Thema [Hecken in Gestaltungsplänen] zusammen.</textarea><br>
                </div>
                
                <div class="form-group">                        
                    <label for="template" class="form-label">Templates</label>
                    <select id="template" class="select-input" name="template" disabled>
                    </select>
                </div>
                
                <div class="form-group">
                    <label class="checkbox-group">
                        <input type="checkbox" id="useFullDocuments" name="useFullDocuments"/>
                        <span class="checkbox-custom"></span>
                        <span>Gesamte Dokument(e) verwenden</span>
                    </label>                
                </div>
                
                <div class="form-group">                        
                    <label for="model" class="form-label">Modell</label>
                    <select id="model"  class="select-input" name="model" disabled>
                        <option value="gpt-4o-mini">gpt-4o-mini</option>
                        <option value="gpt-5-mini">gpt-5-mini</option>
                    </select>                    
                </div>                
            </div>
                    
            <button type="submit" class="btn-input">Antwort generieren</button>
            
        </form>
        <div id="task-panel">
            <p>Wähle zunächst Abschnitte aus der Suche.</p>
        </div>
    </div>
</div>
<script>
    const lexicalWeight = document.getElementById('lexicalWeight');
    const lexicalWeightValue = document.getElementById('lexicalWeightValue');

    if (lexicalWeight && lexicalWeightValue) {
        const updateLexicalLabel = (value) => {
            lexicalWeightValue.textContent = Number(value).toFixed(2);
        };

        lexicalWeight.addEventListener('input', (event) => {
            updateLexicalLabel(event.target.value);
        });

        updateLexicalLabel(lexicalWeight.value);
    }

    const searchResults = document.getElementById('search-results');
    if (searchResults) {
        const setChunkSelection = (scope, checked) => {
            scope.querySelectorAll('.chunk-selection').forEach((checkbox) => {
                checkbox.checked = checked;
            });
        };

        const setSectionSelection = (sectionCard, checked) => {
            setChunkSelection(sectionCard, checked);
            updateSectionState(sectionCard);
        };

        const setDocumentSelection = (documentCard, checked) => {
            documentCard.querySelectorAll('[data-section-card]').forEach((sectionCard) => {
                setSectionSelection(sectionCard, checked);
            });
            const documentToggle = documentCard.querySelector('[data-document-toggle]');
            if (documentToggle) {
                documentToggle.checked = checked;
                documentToggle.indeterminate = false;
            }
            updateDocumentState(documentCard);
        };

        const updateSectionState = (sectionCard) => {
            const sectionToggle = sectionCard.querySelector('[data-section-toggle]');
            const chunkCheckboxes = Array.from(sectionCard.querySelectorAll('.chunk-selection'));
            const checkedChunks = chunkCheckboxes.filter((checkbox) => checkbox.checked);
            if (!sectionToggle) {
                return;
            }
            sectionToggle.checked = chunkCheckboxes.length > 0 && checkedChunks.length === chunkCheckboxes.length;
            sectionToggle.indeterminate = checkedChunks.length > 0 && checkedChunks.length < chunkCheckboxes.length;
        };

        const updateDocumentState = (documentCard) => {
            const documentToggle = documentCard.querySelector('[data-document-toggle]');
            const sectionToggles = Array.from(documentCard.querySelectorAll('[data-section-toggle]'));
            if (!documentToggle || sectionToggles.length === 0) {
                return;
            }
            const allSectionsSelected = sectionToggles.every((toggle) => toggle.checked);
            const anySectionSelected = sectionToggles.some((toggle) => toggle.checked || toggle.indeterminate);
            documentToggle.checked = allSectionsSelected;
            documentToggle.indeterminate = anySectionSelected && !allSectionsSelected;
        };

        const refreshDocumentStates = () => {
            searchResults.querySelectorAll('[data-document-card]').forEach((card) => {
                card.querySelectorAll('[data-section-card]').forEach(updateSectionState);
                updateDocumentState(card);
            });
        };

        searchResults.addEventListener('click', (event) => {
            const target = event.target;
            if (target && target.matches('[data-select-all]')) {
                const sectionCards = Array.from(searchResults.querySelectorAll('[data-section-card]'));
                const sectionToggles = Array.from(searchResults.querySelectorAll('[data-section-toggle]'));
                const allSectionsSelected = sectionToggles.length > 0 && sectionToggles.every((toggle) => toggle.checked);
                const newState = !allSectionsSelected;

                sectionCards.forEach((card) => setSectionSelection(card, newState));

                const selectLabel = target.dataset.labelSelect || 'Alle Abschnitte auswählen';
                const deselectLabel = target.dataset.labelDeselect || 'Alle Abschnitte abwählen';
                target.textContent = newState ? deselectLabel : selectLabel;
                target.setAttribute('aria-pressed', String(newState));
            }
        });

        searchResults.addEventListener('change', (event) => {
            const target = event.target;
            if (target && target.matches('[data-document-toggle]')) {
                const documentCard = target.closest('[data-document-card]');
                if (!documentCard) {
                    return;
                }
                setDocumentSelection(documentCard, target.checked);
            }

            if (target && target.matches('[data-section-toggle]')) {
                const sectionCard = target.closest('[data-section-card]');
                if (!sectionCard) {
                    return;
                }
                setSectionSelection(sectionCard, target.checked);
                const documentCard = sectionCard.closest('[data-document-card]');
                if (documentCard) {
                    updateDocumentState(documentCard);
                }
            }

            if (target && target.matches('.chunk-selection')) {
                const sectionCard = target.closest('[data-section-card]');
                if (sectionCard) {
                    updateSectionState(sectionCard);
                    const documentCard = sectionCard.closest('[data-document-card]');
                    if (documentCard) {
                        updateDocumentState(documentCard);
                    }
                }
            }
        });

        const toggleSubmitDuringRequest = (form, handleAfterRequest) => {
            const submitButton = form ? form.querySelector('button[type="submit"]') : null;
            if (!form || !submitButton) {
                return;
            }

            const setDisabled = (disabled) => {
                submitButton.disabled = disabled;
            };

            form.addEventListener('htmx:beforeRequest', () => setDisabled(true));
            form.addEventListener('htmx:afterRequest', (event) => {
                if (typeof handleAfterRequest === 'function') {
                    handleAfterRequest(event, setDisabled);
                } else {
                    setDisabled(false);
                }
            });
            form.addEventListener('htmx:responseError', () => setDisabled(false));
            form.addEventListener('htmx:sendError', () => setDisabled(false));
        };

        const searchForm = document.querySelector('form[hx-post="/search"]');
        toggleSubmitDuringRequest(searchForm);

        const taskForm = document.querySelector('form[hx-post="/task"]');
        if (taskForm) {
            toggleSubmitDuringRequest(taskForm, (_event, setDisabled) => {
                const taskPanel = document.getElementById('task-panel');
                if (!taskPanel) {
                    setDisabled(false);
                    return;
                }

                const sseContainer = taskPanel.querySelector('[hx-ext~="sse"]');
                if (!sseContainer) {
                    setDisabled(false);
                    return;
                }

                const enableButton = () => {
                    setDisabled(false);
                };

                const handleStreamFinished = () => {
                    sseContainer.removeEventListener('htmx:sseClose', handleStreamFinished);
                    sseContainer.removeEventListener('htmx:sseError', handleStreamFinished);
                    enableButton();
                };

                sseContainer.addEventListener('htmx:sseClose', handleStreamFinished);
                sseContainer.addEventListener('htmx:sseError', handleStreamFinished);
            });
            taskForm.addEventListener('htmx:configRequest', (event) => {
                const sectionToggles = Array.from(searchResults.querySelectorAll('[data-section-toggle]:checked'));

                const sectionIds = sectionToggles
                        .map((toggle) => toggle.value)
                        .filter((value) => value && value.trim() !== '');

                if (sectionIds.length === 0) {
                    event.preventDefault();
                    return;
                }

                const fullDocumentToggle = document.getElementById('useFullDocuments');

                const useFullDocuments = fullDocumentToggle ? fullDocumentToggle.checked : false;

                event.detail.parameters = {
                    ...event.detail.parameters,
                    sectionIds: sectionIds,
                    useFullDocuments: useFullDocuments,
                };
            });
        }
    }
</script>
<script>
  document.querySelectorAll('input[type="range"]').forEach(function (slider) {
    function update() {
      const min = slider.min ? +slider.min : 0;
      const max = slider.max ? +slider.max : 100;
      const val = +slider.value;
      const percent = ((val - min) * 100) / (max - min);
      slider.style.setProperty('--range-progress', percent + '%');
    }
    slider.addEventListener('input', update);
    update(); // initial
  });
</script>
</body>
</html>
