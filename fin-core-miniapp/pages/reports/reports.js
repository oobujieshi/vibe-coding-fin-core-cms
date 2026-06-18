const app = getApp()

Page({
  data: {
    tab: 'income',
    income: { totalIncome: 0, totalExpense: 0, netFlow: 0, items: [] },
    balance: { currentBalance: 0, totalInflow: 0, totalOutflow: 0, transactionCount: 0 },
    settle: { totalOrders: 0, settledCount: 0, pendingCount: 0, closedCount: 0, settledAmount: 0, pendingAmount: 0, paidAmount: 0, receiptAmount: 0 },
  },

  onShow() { this.loadData() },

  loadData() {
    app.request('/reports/income-expense').then(r => this.setData({ income: r })).catch(() => {})
    app.request('/reports/balance').then(r => this.setData({ balance: r })).catch(() => {})
    app.request('/reports/settlement-stats').then(r => this.setData({ settle: r })).catch(() => {})
  },

  switchTab(e) {
    this.setData({ tab: e.currentTarget.dataset.tab })
  },
})
