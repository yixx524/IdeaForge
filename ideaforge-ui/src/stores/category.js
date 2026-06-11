import { defineStore } from 'pinia'
import { listCategories } from '@/api/category'
import { categoryLabel, toCategoryOptions } from '@/constants/categories'

let loadPromise = null

export const useCategoryStore = defineStore('category', {
  state: () => ({
    enabled: [],
    all: [],
    loading: false,
    loaded: false,
    error: null,
  }),

  getters: {
    enabledOptions: (state) => toCategoryOptions(state.enabled),
    allOptions: (state) => toCategoryOptions(state.all),
    labelOf: (state) => (code) => categoryLabel(code, state.all),
  },

  actions: {
    async fetchCategories() {
      this.loading = true
      this.error = null
      try {
        const [enabled, all] = await Promise.all([
          listCategories(),
          listCategories({ all: true }),
        ])
        this.enabled = enabled
        this.all = all
        this.loaded = true
      } catch (e) {
        this.error = e.message
        this.enabled = []
        this.all = []
        this.loaded = false
        throw e
      } finally {
        this.loading = false
        loadPromise = null
      }
    },

    async ensureLoaded() {
      if (this.loaded) return
      if (loadPromise) return loadPromise
      loadPromise = this.fetchCategories().catch(() => {})
      return loadPromise
    },

    async refresh() {
      this.loaded = false
      loadPromise = null
      await this.fetchCategories()
    },

    reset() {
      this.enabled = []
      this.all = []
      this.loading = false
      this.loaded = false
      this.error = null
      loadPromise = null
    },
  },
})
