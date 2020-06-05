// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Switches Light Mode of Page
 */
function switchMode() {
  var style = document.getElementById("pagestyle").getAttribute("href");
  if (style == "") { 
    switchSheet("css/nightmode.css");
    switchIcon("fas fa-sun");
  } else { 
    switchSheet("");
    switchIcon("fas fa-moon");
  }
}

/**
 * Switches Stylesheet of the Page
 */
function switchSheet(mode) { 
  document.getElementById("pagestyle").setAttribute("href", mode);  
}

/**
 * Changes icon for Light Mode button
 */
function switchIcon(icon) { 
  document.getElementById("modeicon").setAttribute("class", icon);  
}

/**
 * get Number of comments from data servlet
 */
async function getNumComments(numComments) {
  const response = await fetch('/data?numComments='.concat(numComments));
  const messages = await response.json();
  const messageContainer = document.getElementById('message-container');
  messageContainer.innerHTML = '';
  for (const message of messages) { 
    messageContainer.appendChild(createListElement(message));
  }
}

/**
 * Display text from data Servlet
 */
async function getText() {
  var numComments = document.getElementById("number-comments");
  numComments = numComments.options[numComments.selectedIndex].value;
  getNumComments(numComments);
}

/** Creates an <li> element containing text. */
function createListElement(message) {
  const liElement = document.createElement('li');
  liElement.className = 'message';
  liElement.innerText = message.text;
  liElement.setAttribute("class", "list-group-item")

  const deleteButtonElement = document.createElement('button');
  deleteButtonElement.innerHTML = '<i class="fas fa-trash-alt" id="modeicon"></i>';
  deleteButtonElement.setAttribute("class", "social-icon float-right");
  deleteButtonElement.addEventListener('click', () => {
    deleteComment(message);

    // Remove the comment from the DOM.
    liElement.remove();
  });

  liElement.appendChild(deleteButtonElement);
  return liElement;
}

/** Tells the server to delete the comment. */
function deleteComment(message) {
  const params = new URLSearchParams();
  params.append('id', message.id);
  fetch('/delete-comment', {method: 'POST', body: params});
}
