const app = getApp()

Page({
  data: { paymentNo: '', amount: '', payee: '', code: '', paymentStatus: 0, statusDesc: '' },

  onScanCode() {
    wx.scanCode({ scanType: ['qrCode'] }).then(res => {
      // Parse QR content: invoice-{paymentId}
      this.setData({ code: res.result })
      const id = res.result.replace('invoice-', '')
      if (id) {
        app.request('/payments/' + id).then(p => {
          this.setData({
            paymentNo: p.paymentNo, amount: p.amount, payee: p.payeeName,
            paymentStatus: p.paymentStatus, statusDesc: p.paymentStatusDesc
          })
        }).catch(() => wx.showToast({ title: '未找到付款信息', icon: 'none' }))
      }
    }).catch(() => {})
  },

  confirmPay() {
    if (!this.data.paymentNo) {
      wx.showToast({ title: '请先扫码', icon: 'none' })
      return
    }
    wx.showModal({
      title: '确认支付',
      content: '支付 ¥' + this.data.amount + ' 给 ' + this.data.payee + '？',
      success: res => {
        if (res.confirm) {
          const id = this.data.code.replace('invoice-', '')
          app.request('/payments/' + id + '/approve', { method: 'PUT', data: { comment: '小程序扫码支付' } })
            .then(() => {
              wx.showToast({ title: '支付确认成功', icon: 'success' })
              this.setData({ paymentNo: '', amount: '', payee: '', code: '' })
            })            .catch(err => wx.showToast({ title: err.message || '确认失败', icon: 'none' }))
        }
      }
    })
  },

  inputCode(e) { this.setData({ code: e.detail.value }) },
  queryById() {
    const id = this.data.code
    if (!id) return wx.showToast({ title: '请输入付款ID', icon: 'none' })
    app.request('/payments/' + id).then(p => {
      this.setData({
        paymentNo: p.paymentNo, amount: p.amount, payee: p.payeeName,
        paymentStatus: p.paymentStatus, statusDesc: p.paymentStatusDesc
      })
    }).catch(() => wx.showToast({ title: '未找到付款信息', icon: 'none' }))
  },
})
