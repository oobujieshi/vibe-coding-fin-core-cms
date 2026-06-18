const app = getApp()

Page({
  data: {
    list: [],
    page: 1,
    total: 0,
    loading: false,
  },

  onShow() {
    this.loadData()
  },

  loadData() {
    this.setData({ loading: true })
    app.request('/bills', { data: { page: this.data.page, size: 20 } })
      .then(res => {
        this.setData({ list: res.records || [], total: res.total, loading: false })
      })
      .catch(() => this.setData({ loading: false }))
  },

  goDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/bill-detail/bill-detail?id=' + id })
  },

  loadMore() {
    if (this.data.list.length >= this.data.total) return
    this.setData({ page: this.data.page + 1 })
    app.request('/bills', { data: { page: this.data.page, size: 20 } })
      .then(res => {
        this.setData({ list: this.data.list.concat(res.records || []) })
      })
  },

  onReachBottom() { this.loadMore() },
  onPullDownRefresh() { this.loadData(); wx.stopPullDownRefresh() },
})
