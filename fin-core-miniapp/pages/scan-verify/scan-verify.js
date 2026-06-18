const app = getApp()

Page({
  data: { receipt: null, verifyCode: '' },

  onScanCode() {
    wx.scanCode({ scanType: ['qrCode'] }).then(res => {
      this.setData({ verifyCode: res.result })
      this.doVerify(res.result)
    }).catch(() => {})
  },

  inputCode(e) { this.setData({ verifyCode: e.detail.value }) },

  doVerify(code) {
    const c = code || this.data.verifyCode
    if (!c) return wx.showToast({ title: '请输入核销码', icon: 'none' })
    app.request('/receipts/verify-by-scan', { method: 'POST', data: { verifyCode: c } })
      .then(res => {
        this.setData({ receipt: res })
        wx.showToast({ title: '核销成功', icon: 'success' })
      })
      .catch(err => {
        wx.showToast({ title: err.message || '核销失败', icon: 'none' })
      })
  },
})
