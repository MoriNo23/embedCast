// ==UserScript==
// @name         EmbedCast Link Extractor
// @namespace    https://github.com/MoriNo23/embedCast
// @version      1.1
// @description  Extract and copy iframe embed URLs with one click. Works on any website with embedded video players.
// @author       mori
// @match        *://*/*
// @grant        none
// @license      MIT
// @icon         https://raw.githubusercontent.com/MoriNo23/embedCast/main/assets/logo.png
// ==/UserScript==

(function() {
    'use strict';

    let intervalId;

    const findAndCopy = () => {
        // Find iframe element (prioritize #iframePlayer, fallback to any iframe)
        const innerIframe = document.querySelector('#iframePlayer') || document.querySelector('iframe');

        if (innerIframe && innerIframe.src && innerIframe.src.startsWith('http')) {
            // If container already exists, stop the loop
            if (document.getElementById('embedcast-copy-container')) {
                clearInterval(intervalId);
                return;
            }

            // Create floating UI container (EmbedCast style)
            const container = document.createElement('div');
            container.id = 'embedcast-copy-container';
            Object.assign(container.style, {
                position: 'fixed',
                bottom: '20px',
                left: '20px',
                zIndex: '999999',
                background: '#1a1a2e',
                border: '2px solid #00ffcc',
                padding: '15px',
                borderRadius: '10px',
                fontFamily: 'Arial, sans-serif',
                boxShadow: '0 4px 15px rgba(0, 255, 204, 0.3)'
            });

            container.innerHTML = `
                <div style="display:flex; align-items:center; margin-bottom:10px;">
                    <img src="https://raw.githubusercontent.com/MoriNo23/embedCast/main/assets/logo.png" 
                         style="width:24px; height:24px; margin-right:8px; border-radius:4px;">
                    <span style="color:#00ffcc; font-weight:bold; font-size:12px;">EmbedCast</span>
                </div>
                <div style="color:#aaa; font-size:10px; margin-bottom:10px; max-width:220px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;" title="${innerIframe.src}">
                    ${innerIframe.src}
                </div>
                <button id="embedcast-btn-copy" style="background:linear-gradient(135deg, #00cc99, #00ffcc); color:#1a1a2e; border:none; padding:10px 20px; font-weight:bold; cursor:pointer; border-radius:5px; width:100%; font-size:12px;">
                    COPY SRC
                </button>
            `;
            document.body.appendChild(container);

            // Copy to clipboard on click
            document.getElementById('embedcast-btn-copy').onclick = function() {
                navigator.clipboard.writeText(innerIframe.src);
                this.innerText = "COPIED!";
                this.style.background = "#00ffcc";
                this.style.color = "#1a1a2e";
            };

            // Stop loop after successful injection
            clearInterval(intervalId);
        }
    };

    // Check every second for iframe availability
    intervalId = setInterval(findAndCopy, 1000);
})();
