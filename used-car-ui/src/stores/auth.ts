// import { defineStore } from 'pinia'
// import { ref } from 'vue'
// import axios from 'axios'
//
// export const useAuthStore = defineStore('auth', () => {
//   const token = ref(localStorage.getItem('token'))
//   const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))
//
//   function setToken(newToken: string) {
//     token.value = newToken
//     localStorage.setItem('token', newToken)
//   }
//
//   function setUser(newUser: any) {
//     user.value = newUser
//     localStorage.setItem('user', JSON.stringify(newUser))
//   }
//
//   async function logout() {
//     try {
//       await axios.post('/api/auth/logout', {}, {
//         headers: { token: token.value }
//       })
//     } catch (error) {
//       console.error('Logout failed:', error)
//     } finally {
//       // 无论后端成功与否，都清除前端状态
//       token.value = null
//       user.value = null
//       localStorage.removeItem('token')
//       localStorage.removeItem('user')
//     }
//   }
//
//   return { token, user, setToken, setUser, logout }
// })
