export interface ContentBlock {
  type: 'text' | 'image'
  content: string
}

export interface Question {
  id: number
  createdBy: string
  orderIndex: number
  questionBlocks: ContentBlock[]
  options: string[]
  answer: string | null
  expectPhoto: boolean
  isBriefing: boolean
  hintBlocks: ContentBlock[]
  explanationBlocks: ContentBlock[]
  mark: number | null
  createdAt: string
  updatedAt: string
  folderId: number | null
}

export interface QuestionRequest {
  questionBlocks?: ContentBlock[]
  orderIndex?: number
  options?: string[]
  answer?: string | null
  expectPhoto?: boolean
  isBriefing?: boolean
  hintBlocks?: ContentBlock[]
  explanationBlocks?: ContentBlock[]
  mark?: number | null
  folderId?: number | null
}

