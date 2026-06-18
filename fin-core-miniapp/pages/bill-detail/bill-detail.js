const app = getApp()

Page({
  data: { detail: {}, id: '' },

  onLoad(options) {
    this.setData({ id: options.id })
    this.loadDetail()
  },

  loadDetail() {
    app.request('/bills/' + this.data.id)
      .then(res => this.setData({ detail: res }))
      .catch(() => wx.showToast({ title: '加载失败', icon: 'none' }))
  },
})
