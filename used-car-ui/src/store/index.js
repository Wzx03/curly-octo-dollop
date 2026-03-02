import { reactive } from 'vue'

export const globalState = reactive({
  showFilter: false,
  searchKeyword: ''
})