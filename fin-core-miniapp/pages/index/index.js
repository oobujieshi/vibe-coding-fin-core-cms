const app = getApp()

Page({
  data: {
    username: '',
    password: '',
    loading: false,
    isLoggedIn: false,
  },

  onShow() {
    const token = wx.getStorageSync('token')
    if (token) this.setData({ isLoggedIn: true })
  },

  onInputUser(e) { this.setData({ username: e.detail.value }) },
  onInputPwd(e) { this.setData({ password: e.detail.value }) },

  login() {
    const { username, password } = this.data
    if (!username || !password) {
      wx.showToast({ title: '请输入用户名和密码', icon: 'none' })
      return
    }
    this.setData({ loading: true })
    app.login(username, password).then(res => {
      wx.setStorageSync('token', res.accessToken)
      wx.setStorageSync('refreshToken', res.refreshToken || '')
      app.globalData.token = res.accessToken
      app.globalData.userInfo = res.userInfo
      this.setData({ isLoggedIn: true, loading: false })
      wx.showToast({ title: '登录成功', icon: 'success' })
    }).catch(err => {
      this.setData({ loading: false })
      wx.showToast({ title: err.message || '登录失败', icon: 'none' })
    })
  },

  logout() {
    wx.removeStorageSync('token')
    wx.removeStorageSync('refreshToken')
    app.globalData.token = null
    this.setData({ isLoggedIn: false })
  },

  goBills() { wx.navigateTo({ url: '/pages/bills/bills' }) },
  goPay() { wx.navigateTo({ url: '/pages/payment/payment' }) },
  goScan() { wx.navigateTo({ url: '/pages/scan-verify/scan-verify' }) },
  goApprovals() { wx.navigateTo({ url: '/pages/approvals/approvals' }) },
  goReports() { wx.navigateTo({ url: '/pages/reports/reports' }) },
})
