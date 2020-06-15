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
  var style = document.getElementById('pagestyle').getAttribute('href');
  if (style == '') { 
    switchSheet('css/nightmode.css');
    switchIcon('fas fa-sun');
  } else { 
    switchSheet('');
    switchIcon('fas fa-moon');
  }
}

/**
 * Switches Stylesheet of the Page
 * @param {string} mode The path to the desired mode stylesheet.
 */
function switchSheet(mode) { 
  document.getElementById('pagestyle').setAttribute('href', mode);  
}

/**
 * Changes icon for Light Mode button
 * @param {string} icon The string code for the fabulous fonts icon to display.
 */
function switchIcon(icon) { 
  document.getElementById('modeicon').setAttribute('class', icon);  
}

/**
 * Get number of comments from data servlet
 * @param {string} numComments A string equivalent of the number of comments in datastore.
 */
async function getNumComments(numComments) {
  const response = await fetch(`/data?numComments=${numComments}`);
  const messages = await response.json();
  const messageContainer = document.getElementById('message-container');
  messageContainer.innerHTML = '';
  document.getElementById('login').setAttribute('href', messages.url);
  if (JSON.parse(messages.loggedin)) { 
    document.getElementById('login').innerHTML = "Logout";
    for (const message of JSON.parse(messages.comments)) { 
      if (messages.user == message.useremail) {
        messageContainer.appendChild(createListElement(message, true));
      } else {
        messageContainer.appendChild(createListElement(message, false));
      }
    }
  } else { 
    document.getElementById('comments-body').innerHTML = `To see and post comments, <a href=${messages.url}>login!</a>`;
    document.getElementById('login').innerHTML = "Login";
  }
}

/**
 * Display text from data Servlet
 */
async function getText() {
  const numComments = document.getElementById('number-comments');
  getNumComments(numComments.options[numComments.selectedIndex].value);
}

/** Creates an <li> element containing text.
 * @param {Object} message A JSON object representing a single comment.
 * @param {Boolean} deletable Whether user can delete comment or not.
 */
function createListElement(message, deletable) {
  const liElement = document.createElement('li');
  liElement.innerHTML = `<b>${message.useremail}:  </b> ${message.text}`;
  liElement.setAttribute('class', 'list-group-item');
  if (deletable) {
    const deleteButtonElement = document.createElement('button');
    deleteButtonElement.innerHTML = '<i class="fas fa-trash-alt" id="modeicon"></i>';
    deleteButtonElement.setAttribute('class', 'social-icon float-right');
    deleteButtonElement.addEventListener('click', () => {
      deleteComment(message);

      // Remove the comment from the DOM.
      liElement.remove();
    });
    liElement.appendChild(deleteButtonElement);
  }
  return liElement;
}

/** 
 * Tells the server to delete the comment. 
 * @param {Object} message A JSON object representing a single comment.
*/
function deleteComment(message) {
  const params = new URLSearchParams();
  params.append('id', message.id);
  fetch('/delete-comment', {method: 'POST', body: params});
}
