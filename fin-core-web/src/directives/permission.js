import { useAuthStore } from '@/stores/auth'

export default {
  mounted(el, binding) {
    const auth = useAuthStore()
    const perm = binding.value
    if (perm && !auth.hasPermission(perm)) {
      el.style.display = 'none'
    }
  }
}
