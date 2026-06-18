const app = getApp()

Page({
  data: { list: [], loading: false },

  onShow() { this.loadData() },

  loadData() {
    this.setData({ loading: true })
    app.request('/approvals/pending', { data: { page: 1, size: 50 } })
      .then(res => this.setData({ list: res.records || [], loading: false }))
      .catch(() => this.setData({ loading: false }))
  },

  approve(e) {
    const { id } = e.currentTarget.dataset
    wx.showToast({ title: '处理中...', icon: 'loading', duration: 2000 })
    wx.showModal({
      title: '审批通过',
      content: '确认通过该审批？',
      success: r => {
        if (r.confirm) {
          app.request('/approvals/' + id + '/process', { method: 'PUT', data: { action: 'APPROVE', comment: '小程序审批通过' } })
            .then(() => { wx.showToast({ title: '已通过', icon: 'success' }); this.loadData() })
            .catch(err => { console.error('审批失败:', err); wx.showToast({ title: err.message || '操作失败', icon: 'none', duration: 3000 }) })
        }
      }
    })
  },

  reject(e) {
    const { id } = e.currentTarget.dataset
    wx.showModal({
      title: '驳回审批',
      editable: true,
      placeholderText: '驳回原因',
      success: r => {
        if (r.confirm) {
          app.request('/approvals/' + id + '/process', { method: 'PUT', data: { action: 'REJECT', comment: r.content || '小程序驳回' } })
            .then(() => { wx.showToast({ title: '已驳回', icon: 'success' }); this.loadData() })
            .catch(err => wx.showToast({ title: err.message || '操作失败', icon: 'none' }))
        }
      }
    })
  },
})
