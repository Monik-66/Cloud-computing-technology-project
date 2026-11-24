const { contextBridge } = require('electron')

contextBridge.exposeInMainWorld('campuslyDesktop', {
  platform: process.platform,
})
