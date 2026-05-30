export interface ContentBlock {
  type: 'text' | 'image'
  content: string
}

export interface Question {
  id: number
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
}

