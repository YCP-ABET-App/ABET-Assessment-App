// frontend/src/composables/use-google-drive.ts
import { ref } from 'vue';

declare global {
  interface Window {
    gapi: any;
    google: any;
  }
}

// Google API configuration
const CLIENT_ID = '343399477878-a8pa2hr24lusvg8plgv79lv7k20lasvb.apps.googleusercontent.com';
const DEVELOPER_KEY = 'AIzaSyBNaSg_OcPlbWfzLzajjryVwrcnNqa5pNA';
const APP_ID = '343399477878';
const SCOPES = [
  'https://www.googleapis.com/auth/drive',
  'https://www.googleapis.com/auth/drive.readonly',
  'https://www.googleapis.com/auth/drive.file'
];

let tokenClient: any = null;
let accessToken: string | null = null;

function loadScript(src: string): Promise<void> {
  return new Promise((resolve, reject) => {
    if (document.querySelector(`script[src="${src}"]`)) {
      resolve();
      return;
    }
    const script = document.createElement('script');
    script.src = src;
    script.async = true;
    script.onload = () => resolve();
    script.onerror = () => reject(new Error(`Failed to load script: ${src}`));
    document.body.appendChild(script);
  });
}

async function ensurePickerAndGISLoaded() {
  await loadScript('https://accounts.google.com/gsi/client');
  await loadScript('https://apis.google.com/js/api.js');
  await new Promise((resolve) => {
    if (window.google && window.google.picker) {
      resolve(true);
      return;
    }
    window.gapi.load('picker', { callback: resolve });
  });
}

async function openDriveFolderPickerAndUpload(blob: Blob, filename: string) {
  await ensurePickerAndGISLoaded();

  return new Promise<void>((resolve, reject) => {
    if (!tokenClient) {
      tokenClient = window.google.accounts.oauth2.initTokenClient({
        client_id: CLIENT_ID,
        scope: SCOPES.join(' '),
        callback: (tokenResponse: any) => {
          if (tokenResponse && tokenResponse.access_token) {
            accessToken = tokenResponse.access_token;
            if (accessToken) {
                showFolderPickerAndUpload(accessToken, blob, filename, resolve, reject);
            } else {
                reject('Failed to get access token');
            }
          } else {
            reject('Failed to get access token');
          }
        }
      });
    }
    tokenClient.requestAccessToken();
  });
}

function showFolderPickerAndUpload(oauthToken: string, blob: Blob, filename: string, resolve: () => void, reject: (err: any) => void) {
  const folderView = new window.google.picker.DocsView(window.google.picker.ViewId.FOLDERS)
    .setIncludeFolders(true)
    .setSelectFolderEnabled(true)
    .setMimeTypes('application/vnd.google-apps.folder');
  const picker = new window.google.picker.PickerBuilder()
    .addView(folderView)
    .setOAuthToken(oauthToken)
    .setDeveloperKey(DEVELOPER_KEY)
    .setAppId(APP_ID)
    .setCallback(async (data: any) => {
      if (data.action === window.google.picker.Action.PICKED) {
        const folderId = data.docs[0].id;
        try {
          await uploadPdfToFolder(folderId, oauthToken, blob, filename);
          resolve();
        } catch (e) {
          reject(e);
        }
      } else if (data.action === window.google.picker.Action.CANCEL) {
        reject('Folder selection cancelled');
      }
    })
    .enableFeature(window.google.picker.Feature.SUPPORT_DRIVES)
    .enableFeature(window.google.picker.Feature.SELECT_FOLDER)
    .enableFeature(window.google.picker.Feature.MULTISELECT_DISABLED)
    .build();
  picker.setVisible(true);
}

async function uploadPdfToFolder(folderId: string, oauthToken: string, blob: Blob, filename: string) {
  const metadata = {
    name: filename,
    mimeType: 'application/pdf',
    parents: [folderId]
  };

  if (!window.gapi.client) {
    await new Promise((resolve) => window.gapi.load('client', { callback: resolve }));
  }
  await window.gapi.client.init({
    apiKey: DEVELOPER_KEY,
    discoveryDocs: [
      'https://www.googleapis.com/discovery/v1/apis/drive/v3/rest'
    ]
  });

  const form = new FormData();
  form.append('metadata', new Blob([JSON.stringify(metadata)], { type: 'application/json' }));
  form.append('file', blob);

  const response = await fetch('https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart', {
    method: 'POST',
    headers: {
      'Authorization': 'Bearer ' + oauthToken
    },
    body: form
  });
  if (!response.ok) {
    const err = await response.json();
    throw new Error('Upload failed: ' + JSON.stringify(err));
  }
}

export function useGoogleDrive() {
  return {
    openDriveFolderPickerAndUpload
  };
}