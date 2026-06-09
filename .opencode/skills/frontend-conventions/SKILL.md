---
name: frontend-conventions
description: Use when creating or modifying frontend Vue 3 + TypeScript code. Covers component structure, Pinia store patterns, service layer conventions, TypeScript types, Tailwind styling conventions, and Vue Router configuration for this project.
---

# Frontend Conventions

## Component Structure

```vue
<template>
  <AppLayout>
    <div class="min-h-screen bg-slate-50">
      <!-- Content -->
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppLayout from '@/components/AppLayout.vue'
import myService from '@/services/myService'
import type { MyType } from '@/types/myType'

const router = useRouter()

const items = ref<MyType[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

const filteredItems = computed(() => 
  items.value.filter(item => item.active)
)

async function loadData() {
  loading.value = true
  error.value = null
  try {
    items.value = await myService.list()
  } catch (e: any) {
    error.value = e.response?.data?.message || 'Failed to load'
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
```

## Service Layer

```typescript
// src/services/myService.ts
import api from './api'
import type { MyRequest, MyResponse } from '@/types/myType'

const myService = {
  list: async (): Promise<MyResponse[]> => {
    const { data } = await api.get('/api/resources')
    return data
  },

  getById: async (id: number): Promise<MyResponse> => {
    const { data } = await api.get(`/api/resources/${id}`)
    return data
  },

  create: async (request: MyRequest): Promise<MyResponse> => {
    const { data } = await api.post('/api/resources', request)
    return data
  },

  update: async (id: number, request: MyRequest): Promise<MyResponse> => {
    const { data } = await api.put(`/api/resources/${id}`, request)
    return data
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/api/resources/${id}`)
  }
}

export default myService
```

## TypeScript Types

```typescript
// src/types/myType.ts
export interface MyRequest {
  name: string
  description?: string
  relatedId: number
}

export interface MyResponse {
  id: number
  name: string
  description: string | null
  relatedId: number
  createdAt: string
}
```

## Pinia Store (Composition API)

```typescript
// src/stores/myStore.ts
import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useMyStore = defineStore('myStore', () => {
  const items = ref<MyType[]>([])
  const selectedId = ref<number | null>(null)

  const selectedItem = computed(() => 
    items.value.find(item => item.id === selectedId.value)
  )

  function addItem(item: MyType) {
    items.value.push(item)
  }

  function clear() {
    items.value = []
    selectedId.value = null
  }

  return { items, selectedId, selectedItem, addItem, clear }
})
```

## Props and Emits

```vue
<script setup lang="ts">
const props = defineProps<{
  item: MyType
  readonly?: boolean
}>()

const emit = defineEmits<{
  update: [item: MyType]
  delete: [id: number]
}>()
</script>
```

## Modal Pattern

```vue
<template>
  <teleport to="body">
    <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center">
      <div class="fixed inset-0 bg-black/50" @click="close" />
      <div class="relative z-10 bg-white rounded-2xl shadow-xl max-w-2xl w-full mx-4 p-6">
        <h2 class="text-xl font-bold text-slate-900 mb-4">{{ title }}</h2>
        <slot />
        <div class="flex justify-end gap-3 mt-6">
          <button @click="close" class="px-4 py-2 text-slate-600 hover:bg-slate-100 rounded-lg">
            Cancel
          </button>
          <button @click="confirm" class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">
            Confirm
          </button>
        </div>
      </div>
    </div>
  </teleport>
</template>
```

## Tailwind Styling Conventions

**Color palette:**
- Neutrals: `slate-50` to `slate-900`
- Primary: `blue-600` (hover: `blue-700`)
- Destructive: `red-500` / `red-600`
- Success: `green-500` / `green-600`
- Warning: `amber-500` / `amber-600`

**Common patterns:**
```html
<!-- Card -->
<div class="bg-white rounded-xl shadow-sm border border-slate-200 p-6">

<!-- Button (primary) -->
<button class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium">

<!-- Button (secondary) -->
<button class="px-4 py-2 text-slate-600 hover:bg-slate-100 rounded-lg font-medium">

<!-- Input -->
<input class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500">

<!-- Badge -->
<span class="px-2 py-1 text-xs font-medium rounded-full bg-green-100 text-green-700">

<!-- Table -->
<table class="w-full">
  <thead class="bg-slate-50 border-b border-slate-200">
    <th class="px-4 py-3 text-left text-xs font-semibold text-slate-600 uppercase">
  </thead>
  <tbody class="divide-y divide-slate-200">
    <td class="px-4 py-3 text-sm text-slate-900">
  </tbody>
</table>
```

## Drag and Drop

**List reordering (vue-draggable-plus):**
```vue
<template>
  <draggable v-model="items" item-key="id" @end="onReorder">
    <template #item="{ element }">
      <div class="cursor-move">{{ element.name }}</div>
    </template>
  </draggable>
</template>

<script setup lang="ts">
import { draggable } from 'vue-draggable-plus'
</script>
```

**Cross-container (native HTML5 DnD):**
```vue
<template>
  <div draggable="true" @dragstart="onDragStart($event, item)">...</div>
  <div @dragover.prevent @drop="onDrop($event)">...</div>
</template>
```

## Router Navigation

```typescript
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

// Navigate
router.push('/questions')
router.push({ name: 'quiz-detail', params: { id: 123 } })

// Get params
const id = Number(route.params.id)
```

## Error Handling

```typescript
try {
  await myService.delete(id)
} catch (e: any) {
  const message = e.response?.data?.message || 'Operation failed'
  errorDialog.value = { visible: true, message }
}
```

## Loading States

```vue
<template>
  <div v-if="loading" class="animate-pulse space-y-3">
    <div class="h-4 bg-slate-200 rounded w-3/4" />
    <div class="h-4 bg-slate-200 rounded w-1/2" />
  </div>
  <div v-else>
    <!-- Actual content -->
  </div>
</template>
```
