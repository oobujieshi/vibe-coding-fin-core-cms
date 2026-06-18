App({
  onLaunch() {
    const token = wx.getStorageSync('token')
    if (token) this.globalData.token = token
  },

  globalData: {
    userInfo: null,
    token: null,
    host: 'http://172.28.128.1',
  },

  // 根据 API 路径路由到正确的微服务端口
  getUrl(path) {
    const p = this.globalData.host
    if (/^\/(auth|orders|fee-rules|settlements)/.test(path)) return p + ':8081/api/v1' + path
    if (/^\/(receipts|payments|bills|approvals)/.test(path)) return p + ':8082/api/v1' + path
    if (/^\/(accounts|transactions|reconciliations)/.test(path)) return p + ':8083/api/v1' + path
    if (/^\/reports/.test(path)) return p + ':8084/api/v1' + path
    return p + ':8081/api/v1' + path
  },

  request(path, options = {}) {
    return new Promise((resolve, reject) => {
      const token = wx.getStorageSync('token') || this.globalData.token
      wx.request({
        url: this.getUrl(path),
        method: options.method || 'GET',
        data: options.data,
        header: {
          'Content-Type': 'application/json',
          ...(token ? { 'Authorization': 'Bearer ' + token } : {}),
        },
        success(res) {
          if (res.statusCode === 200 && res.data.code === 200) {
            resolve(res.data.data)
          } else if (res.statusCode === 401) {
            wx.removeStorageSync('token')
            wx.redirectTo({ url: '/pages/index/index' })
            reject(new Error('未登录'))
          } else {
            reject(new Error(res.data.message || '请求失败'))
          }
        },
        fail(err) { reject(err) }
      })
    })
  },

  login(username, password) {
    return this.request('/auth/login', { method: 'POST', data: { username, password } })
  }
})
