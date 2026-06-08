<template>
  <teleport to="body">
    <transition
      enter-active-class="transition-opacity duration-150"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-150"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <!-- Backdrop -->
        <div class="absolute inset-0 bg-black/50" @click="$emit('close')" />

        <!-- Modal panel -->
        <div
          class="relative bg-white rounded-2xl shadow-2xl flex flex-col w-full overflow-hidden"
          style="width: 90vw; max-width: 90vw; height: 90vh; max-height: 90vh"
          role="dialog"
          aria-modal="true"
        >

          <!-- Header -->
          <div class="flex items-center justify-between px-6 py-4 border-b border-slate-100">
            <div class="flex items-center gap-3">
              <div class="w-9 h-9 rounded-xl bg-blue-600 flex items-center justify-center text-white font-bold text-base shrink-0">
                {{ isEdit ? '✎' : '+' }}
              </div>
              <h2 class="text-lg font-bold text-slate-900 leading-tight">{{ isEdit ? 'Edit Question' : 'New Question' }}</h2>
            </div>
            <button
              type="button"
              class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition cursor-pointer"
              aria-label="Close"
              @click="$emit('close')"
            >✕</button>
          </div>

          <!-- Body -->
          <div class="flex flex-1 overflow-hidden min-h-0">
            <!-- Form panel -->
            <div class="w-1/2 min-w-0 flex flex-col border-r border-slate-100">

              <!-- Question Type (always visible) -->
              <div class="px-6 pt-5 pb-4 border-b border-slate-100 shrink-0">
                <div class="space-y-2">
                  <label class="block text-sm font-semibold text-slate-700">Question Type</label>
                  <div class="grid grid-cols-3 gap-3">
                    <button
                      type="button"
                      class="flex flex-col items-center gap-2 px-3 py-4 rounded-xl border-2 transition cursor-pointer text-center"
                      :class="!form.expectPhoto && !form.isBriefing
                        ? 'border-blue-500 bg-blue-50 shadow-sm'
                        : 'border-slate-200 bg-slate-50 hover:border-slate-300 hover:bg-white'"
                      @click="setQuestionType(false, false)"
                    >
                      <span class="text-2xl leading-none">⌨️</span>
                      <span class="text-sm font-bold" :class="!form.expectPhoto && !form.isBriefing ? 'text-blue-700' : 'text-slate-800'">Multiple Choice</span>
                      <span class="text-xs text-slate-500 leading-snug">Players pick from A / B / C / D options</span>
                    </button>
                    <button
                      type="button"
                      class="flex flex-col items-center gap-2 px-3 py-4 rounded-xl border-2 transition cursor-pointer text-center"
                      :class="form.expectPhoto && !form.isBriefing
                        ? 'border-blue-500 bg-blue-50 shadow-sm'
                        : 'border-slate-200 bg-slate-50 hover:border-slate-300 hover:bg-white'"
                      @click="setQuestionType(true, false)"
                    >
                      <span class="text-2xl leading-none">📷</span>
                      <span class="text-sm font-bold" :class="form.expectPhoto && !form.isBriefing ? 'text-blue-700' : 'text-slate-800'">Photo</span>
                      <span class="text-xs text-slate-500 leading-snug">Players send a photo as their answer</span>
                    </button>
                    <button
                      type="button"
                      class="flex flex-col items-center gap-2 px-3 py-4 rounded-xl border-2 transition cursor-pointer text-center"
                      :class="form.isBriefing
                        ? 'border-blue-500 bg-blue-50 shadow-sm'
                        : 'border-slate-200 bg-slate-50 hover:border-slate-300 hover:bg-white'"
                      @click="setQuestionType(false, true)"
                    >
                      <span class="text-2xl leading-none">📋</span>
                      <span class="text-sm font-bold" :class="form.isBriefing ? 'text-blue-700' : 'text-slate-800'">Briefing</span>
                      <span class="text-xs text-slate-500 leading-snug">Instructions + photo, then READY</span>
                    </button>
                  </div>
                </div>
              </div>

              <!-- Folder picker -->
              <div v-if="props.folders.length > 0" class="flex items-center gap-3 px-5 py-2 border-b border-slate-100 bg-slate-50/40 shrink-0">
                <span class="text-[0.7rem] font-semibold text-slate-400 uppercase tracking-wide flex-shrink-0">Folder</span>
                <select
                  v-model="form.folderId"
                  class="flex-1 text-xs border border-slate-200 rounded-lg px-2 py-1.5 bg-white text-slate-700 outline-none focus:border-blue-400 cursor-pointer transition"
                >
                  <option :value="null">— No folder —</option>
                  <option v-for="f in props.folders" :key="f.id" :value="f.id">{{ f.name }}</option>
                </select>
              </div>

              <!-- Sub-tabs -->
              <div class="flex gap-1 border-b border-slate-200 px-4 pt-1 shrink-0">
                <button
                  v-for="tab in tabs"
                  :key="tab.value"
                  type="button"
                  class="px-4 py-2 text-sm font-semibold rounded-t-lg border border-b-0 -mb-px transition cursor-pointer"
                  :class="activeTab === tab.value
                    ? 'bg-white border-slate-200 text-blue-600 shadow-sm'
                    : 'bg-slate-50 border-transparent text-slate-500 hover:text-slate-700 hover:bg-slate-100'"
                  @click="activeTab = tab.value"
                >{{ tab.label }}</button>
              </div>

              <!-- Tab content -->
              <div class="flex-1 overflow-y-auto" style="scrollbar-gutter: stable">
                <form @submit.prevent="submit" class="p-6 space-y-5">

                  <!-- ── Tab: Question (Multiple Choice / Photo) ── -->
                  <template v-if="activeTab === 'question'">

                    <!-- Question Blocks -->
                    <div class="space-y-1.5">
                      <label class="block text-sm font-semibold text-slate-700">
                        Question Content <span class="text-red-500">*</span>
                      </label>
                      <VueDraggable v-model="form.questionBlocks" :animation="150" handle=".drag-handle" class="space-y-2">
                        <div v-for="block in form.questionBlocks" :key="block._id" class="rounded-lg border border-slate-200 bg-white overflow-hidden">
                          <div class="flex items-center gap-1.5 px-2.5 py-1.5 bg-slate-50 border-b border-slate-100">
                            <div class="drag-handle cursor-grab text-slate-300 hover:text-slate-500 select-none text-base leading-none px-0.5" title="Drag to reorder">⠿</div>
                            <span class="text-xs font-semibold" :class="block.type === 'text' ? 'text-blue-600' : 'text-amber-600'">{{ block.type === 'text' ? 'Text' : 'Image' }}</span>
                            <div class="flex-1"></div>
                            <button type="button" class="text-slate-300 hover:text-red-500 transition text-xs leading-none cursor-pointer px-1" @click="removeBlock(form.questionBlocks, block._id)" title="Remove">✕</button>
                          </div>
                          <div v-if="block.type === 'text'" class="p-3 space-y-1.5">
                            <div class="flex items-center gap-0.5 px-1.5 py-1 bg-slate-50 border border-slate-200 rounded-lg w-fit">
                              <button type="button" class="px-1.5 py-0.5 text-xs font-bold text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(questionTextareas[block._id] ?? null, form.questionBlocks, block._id, '<b>', '</b>')">B</button>
                              <button type="button" class="px-1.5 py-0.5 text-xs italic text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(questionTextareas[block._id] ?? null, form.questionBlocks, block._id, '<i>', '</i>')">I</button>
                              <button type="button" class="px-1.5 py-0.5 text-xs underline text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(questionTextareas[block._id] ?? null, form.questionBlocks, block._id, '<u>', '</u>')">U</button>
                              <button type="button" class="px-1.5 py-0.5 text-xs line-through text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(questionTextareas[block._id] ?? null, form.questionBlocks, block._id, '<s>', '</s>')">S</button>
                              <div class="w-px h-3 bg-slate-200 mx-0.5"></div>
                              <button type="button" class="px-1.5 py-0.5 text-xs font-mono text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(questionTextareas[block._id] ?? null, form.questionBlocks, block._id, '<code>', '</code>')">&lt;/&gt;</button>
                              <div class="w-px h-3 bg-slate-200 mx-0.5"></div>
                              <button v-for="c in COLORS" :key="c.hex" type="button" :title="c.label" :style="{ backgroundColor: c.hex }" class="w-3.5 h-3.5 rounded-full cursor-pointer hover:scale-110 transition-transform flex-shrink-0" @click="applyBlockFormat(questionTextareas[block._id] ?? null, form.questionBlocks, block._id, colorTag(c.hex), '</span>')"></button>
                            </div>
                            <textarea
                              :ref="(el) => { questionTextareas[block._id] = el as HTMLTextAreaElement }"
                              v-model="block.content"
                              rows="2"
                              class="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 resize-none transition bg-white text-slate-900 placeholder-slate-400 font-[inherit]"
                              placeholder="Type here…"
                            ></textarea>
                          </div>
                          <div v-else class="p-3">
                            <div v-if="block.content" class="relative rounded-lg overflow-hidden">
                              <img :src="`/api/files/${block.content}`" class="max-h-48 w-full object-contain block rounded-lg bg-slate-100" alt="" />
                              <button type="button" class="absolute top-1.5 right-1.5 w-6 h-6 rounded-full bg-black/55 text-white border-none text-[0.7rem] cursor-pointer flex items-center justify-center leading-none hover:bg-red-600/90 transition-colors" @click="block.content = ''" title="Remove">✕</button>
                            </div>
                            <div v-else class="border-2 border-dashed rounded-xl p-5 flex items-center justify-center cursor-pointer transition select-none border-slate-200 bg-slate-50 hover:border-amber-400 hover:bg-amber-50" @click="uploadImageBlock(block)">
                              <div class="flex flex-col items-center gap-1 pointer-events-none">
                                <div class="text-3xl leading-none">🖼️</div>
                                <div class="text-sm font-semibold text-slate-600">Click to upload</div>
                                <div class="text-xs text-slate-400">or drag &amp; drop a file</div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </VueDraggable>
                      <div class="flex gap-2">
                        <button type="button" class="flex-1 py-2 border border-dashed border-slate-300 rounded-lg text-xs text-slate-400 hover:border-blue-400 hover:text-blue-500 transition cursor-pointer" @click="addTextBlock(form.questionBlocks)">+ Add text</button>
                        <button type="button" class="flex-1 py-2 border border-dashed border-amber-200 rounded-lg text-xs text-amber-400 hover:border-amber-400 hover:text-amber-500 transition cursor-pointer" @click="addImageBlock(form.questionBlocks)">+ Add image</button>
                      </div>
                    </div>

                  </template>

                  <!-- ── Tab: Answer (Multiple Choice only) ── -->
                  <template v-if="activeTab === 'answer'">

                    <div class="space-y-1.5">
                      <label class="block text-sm font-semibold text-slate-700">Answer Options</label>
                      <div class="space-y-2">
                        <div
                          v-for="(opt, i) in form.options"
                          :key="i"
                          class="flex items-center gap-2 px-3 py-2 rounded-lg border transition"
                          :class="form.answer && form.answer === opt
                            ? 'border-green-300 bg-green-50'
                            : 'border-slate-200 bg-white hover:border-slate-300'"
                        >
                          <span
                            class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold shrink-0"
                            :class="form.answer && form.answer === opt ? 'bg-green-100 text-green-700' : 'bg-blue-50 text-blue-600'"
                          >{{ String.fromCharCode(65 + i) }}</span>
                          <input
                            v-model="form.options[i]"
                            class="flex-1 text-sm outline-none bg-transparent text-slate-900 placeholder-slate-400"
                            :placeholder="`Option ${String.fromCharCode(65 + i)}`"
                          />
                          <button type="button" class="text-slate-300 hover:text-red-500 transition text-xs px-1 cursor-pointer leading-none" @click="removeOption(i)" title="Remove">✕</button>
                        </div>
                        <button
                          v-if="form.options.length < 4"
                          type="button"
                          class="w-full py-2 border border-dashed border-slate-300 rounded-lg text-xs text-slate-400 hover:border-blue-400 hover:text-blue-500 transition cursor-pointer"
                          @click="addOption"
                        >+ Add option</button>
                      </div>
                    </div>

                    <div class="space-y-1.5">
                      <label class="block text-sm font-semibold text-slate-700">Correct Answer</label>
                      <select
                        v-model="form.answer"
                        class="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 transition bg-white text-slate-900 cursor-pointer"
                      >
                        <option value="">— select —</option>
                        <option v-for="(opt, i) in form.options" :key="i" :value="opt">
                          {{ String.fromCharCode(65 + i) }}: {{ opt || '(empty)' }}
                        </option>
                      </select>
                    </div>

                  </template>

                  <!-- ── Tab: Mark ── -->
                  <template v-if="activeTab === 'mark'">

                    <div class="space-y-4">
                      <div class="space-y-1.5">
                        <label class="block text-sm font-semibold text-slate-700">Points / Mark</label>
                        <p class="text-xs text-slate-400">How many points this question is worth. Leave blank for unscored.</p>
                        <div class="flex items-center gap-3">
                          <input
                            v-model.number="form.mark"
                            type="number"
                            min="0"
                            step="1"
                            class="w-32 px-3 py-2 text-sm border border-slate-200 rounded-lg outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 transition bg-white text-slate-900 placeholder-slate-400"
                            placeholder="e.g. 1"
                          />
                          <button
                            v-if="form.mark !== null"
                            type="button"
                            class="text-xs text-slate-400 hover:text-red-500 transition cursor-pointer"
                            @click="form.mark = null"
                          >Clear</button>
                        </div>
                      </div>
                      <div class="flex flex-wrap gap-2">
                        <span class="text-xs text-slate-500 self-center">Quick set:</span>
                        <button v-for="n in [1, 2, 3, 5, 10]" :key="n" type="button"
                          class="px-3 py-1 rounded-full text-xs font-semibold border transition cursor-pointer"
                          :class="form.mark === n
                            ? 'bg-blue-600 text-white border-blue-600'
                            : 'bg-white text-slate-600 border-slate-200 hover:border-blue-400 hover:text-blue-600'"
                          @click="form.mark = n"
                        >{{ n }}</button>
                      </div>
                    </div>

                  </template>

                  <!-- ── Tab: Hint ── -->
                  <template v-if="activeTab === 'hint'">

                    <div class="space-y-1.5">
                      <label class="block text-sm font-semibold text-slate-700">Hint Content</label>
                      <VueDraggable v-model="form.hintBlocks" :animation="150" handle=".drag-handle" class="space-y-2">
                        <div v-for="block in form.hintBlocks" :key="block._id" class="rounded-lg border border-slate-200 bg-white overflow-hidden">
                          <div class="flex items-center gap-1.5 px-2.5 py-1.5 bg-slate-50 border-b border-slate-100">
                            <div class="drag-handle cursor-grab text-slate-300 hover:text-slate-500 select-none text-base leading-none px-0.5" title="Drag to reorder">⠿</div>
                            <span class="text-xs font-semibold" :class="block.type === 'text' ? 'text-blue-600' : 'text-amber-600'">{{ block.type === 'text' ? 'Text' : 'Image' }}</span>
                            <div class="flex-1"></div>
                            <button type="button" class="text-slate-300 hover:text-red-500 transition text-xs leading-none cursor-pointer px-1" @click="removeBlock(form.hintBlocks, block._id)" title="Remove">✕</button>
                          </div>
                          <div v-if="block.type === 'text'" class="p-3 space-y-1.5">
                            <div class="flex items-center gap-0.5 px-1.5 py-1 bg-slate-50 border border-slate-200 rounded-lg w-fit">
                              <button type="button" class="px-1.5 py-0.5 text-xs font-bold text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(hintTextareas[block._id] ?? null, form.hintBlocks, block._id, '<b>', '</b>')">B</button>
                              <button type="button" class="px-1.5 py-0.5 text-xs italic text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(hintTextareas[block._id] ?? null, form.hintBlocks, block._id, '<i>', '</i>')">I</button>
                              <button type="button" class="px-1.5 py-0.5 text-xs underline text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(hintTextareas[block._id] ?? null, form.hintBlocks, block._id, '<u>', '</u>')">U</button>
                              <button type="button" class="px-1.5 py-0.5 text-xs line-through text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(hintTextareas[block._id] ?? null, form.hintBlocks, block._id, '<s>', '</s>')">S</button>
                              <div class="w-px h-3 bg-slate-200 mx-0.5"></div>
                              <button type="button" class="px-1.5 py-0.5 text-xs font-mono text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(hintTextareas[block._id] ?? null, form.hintBlocks, block._id, '<code>', '</code>')">&lt;/&gt;</button>
                              <div class="w-px h-3 bg-slate-200 mx-0.5"></div>
                              <button v-for="c in COLORS" :key="c.hex" type="button" :title="c.label" :style="{ backgroundColor: c.hex }" class="w-3.5 h-3.5 rounded-full cursor-pointer hover:scale-110 transition-transform flex-shrink-0" @click="applyBlockFormat(hintTextareas[block._id] ?? null, form.hintBlocks, block._id, colorTag(c.hex), '</span>')"></button>
                            </div>
                            <textarea
                              :ref="(el) => { hintTextareas[block._id] = el as HTMLTextAreaElement }"
                              v-model="block.content"
                              rows="2"
                              class="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 resize-none transition bg-white text-slate-900 placeholder-slate-400 font-[inherit]"
                              placeholder="Hint text…"
                            ></textarea>
                          </div>
                          <div v-else class="p-3">
                            <div v-if="block.content" class="relative rounded-lg overflow-hidden">
                              <img :src="`/api/files/${block.content}`" class="max-h-48 w-full object-contain block rounded-lg bg-slate-100" alt="" />
                              <button type="button" class="absolute top-1.5 right-1.5 w-6 h-6 rounded-full bg-black/55 text-white border-none text-[0.7rem] cursor-pointer flex items-center justify-center leading-none hover:bg-red-600/90 transition-colors" @click="block.content = ''" title="Remove">✕</button>
                            </div>
                            <div v-else class="border-2 border-dashed rounded-xl p-5 flex items-center justify-center cursor-pointer transition select-none border-slate-200 bg-slate-50 hover:border-amber-400 hover:bg-amber-50" @click="uploadImageBlock(block)">
                              <div class="flex flex-col items-center gap-1 pointer-events-none">
                                <div class="text-3xl leading-none">🖼️</div>
                                <div class="text-sm font-semibold text-slate-600">Click to upload</div>
                                <div class="text-xs text-slate-400">or drag &amp; drop a file</div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </VueDraggable>
                      <div class="flex gap-2">
                        <button type="button" class="flex-1 py-2 border border-dashed border-slate-300 rounded-lg text-xs text-slate-400 hover:border-blue-400 hover:text-blue-500 transition cursor-pointer" @click="addTextBlock(form.hintBlocks)">+ Add text</button>
                        <button type="button" class="flex-1 py-2 border border-dashed border-amber-200 rounded-lg text-xs text-amber-400 hover:border-amber-400 hover:text-amber-500 transition cursor-pointer" @click="addImageBlock(form.hintBlocks)">+ Add image</button>
                      </div>
                    </div>

                  </template>

                  <!-- ── Tab: After Answer ── -->
                  <template v-if="activeTab === 'explanation'">

                    <div class="space-y-1.5">
                      <label class="block text-sm font-semibold text-slate-700">After Answer Content</label>
                      <VueDraggable v-model="form.explanationBlocks" :animation="150" handle=".drag-handle" class="space-y-2">
                        <div v-for="block in form.explanationBlocks" :key="block._id" class="rounded-lg border border-slate-200 bg-white overflow-hidden">
                          <div class="flex items-center gap-1.5 px-2.5 py-1.5 bg-slate-50 border-b border-slate-100">
                            <div class="drag-handle cursor-grab text-slate-300 hover:text-slate-500 select-none text-base leading-none px-0.5" title="Drag to reorder">⠿</div>
                            <span class="text-xs font-semibold" :class="block.type === 'text' ? 'text-blue-600' : 'text-amber-600'">{{ block.type === 'text' ? 'Text' : 'Image' }}</span>
                            <div class="flex-1"></div>
                            <button type="button" class="text-slate-300 hover:text-red-500 transition text-xs leading-none cursor-pointer px-1" @click="removeBlock(form.explanationBlocks, block._id)" title="Remove">✕</button>
                          </div>
                          <div v-if="block.type === 'text'" class="p-3 space-y-1.5">
                            <div class="flex items-center gap-0.5 px-1.5 py-1 bg-slate-50 border border-slate-200 rounded-lg w-fit">
                              <button type="button" class="px-1.5 py-0.5 text-xs font-bold text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(explanationTextareas[block._id] ?? null, form.explanationBlocks, block._id, '<b>', '</b>')">B</button>
                              <button type="button" class="px-1.5 py-0.5 text-xs italic text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(explanationTextareas[block._id] ?? null, form.explanationBlocks, block._id, '<i>', '</i>')">I</button>
                              <button type="button" class="px-1.5 py-0.5 text-xs underline text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(explanationTextareas[block._id] ?? null, form.explanationBlocks, block._id, '<u>', '</u>')">U</button>
                              <button type="button" class="px-1.5 py-0.5 text-xs line-through text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(explanationTextareas[block._id] ?? null, form.explanationBlocks, block._id, '<s>', '</s>')">S</button>
                              <div class="w-px h-3 bg-slate-200 mx-0.5"></div>
                              <button type="button" class="px-1.5 py-0.5 text-xs font-mono text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(explanationTextareas[block._id] ?? null, form.explanationBlocks, block._id, '<code>', '</code>')">&lt;/&gt;</button>
                              <div class="w-px h-3 bg-slate-200 mx-0.5"></div>
                              <button v-for="c in COLORS" :key="c.hex" type="button" :title="c.label" :style="{ backgroundColor: c.hex }" class="w-3.5 h-3.5 rounded-full cursor-pointer hover:scale-110 transition-transform flex-shrink-0" @click="applyBlockFormat(explanationTextareas[block._id] ?? null, form.explanationBlocks, block._id, colorTag(c.hex), '</span>')"></button>
                            </div>
                            <textarea
                              :ref="(el) => { explanationTextareas[block._id] = el as HTMLTextAreaElement }"
                              v-model="block.content"
                              rows="3"
                              class="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 resize-none transition bg-white text-slate-900 placeholder-slate-400 font-[inherit]"
                              placeholder="After answer text…"
                            ></textarea>
                          </div>
                          <div v-else class="p-3">
                            <div v-if="block.content" class="relative rounded-lg overflow-hidden">
                              <img :src="`/api/files/${block.content}`" class="max-h-48 w-full object-contain block rounded-lg bg-slate-100" alt="" />
                              <button type="button" class="absolute top-1.5 right-1.5 w-6 h-6 rounded-full bg-black/55 text-white border-none text-[0.7rem] cursor-pointer flex items-center justify-center leading-none hover:bg-red-600/90 transition-colors" @click="block.content = ''" title="Remove">✕</button>
                            </div>
                            <div v-else class="border-2 border-dashed rounded-xl p-5 flex items-center justify-center cursor-pointer transition select-none border-slate-200 bg-slate-50 hover:border-amber-400 hover:bg-amber-50" @click="uploadImageBlock(block)">
                              <div class="flex flex-col items-center gap-1 pointer-events-none">
                                <div class="text-3xl leading-none">🖼️</div>
                                <div class="text-sm font-semibold text-slate-600">Click to upload</div>
                                <div class="text-xs text-slate-400">or drag &amp; drop a file</div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </VueDraggable>
                      <div class="flex gap-2">
                        <button type="button" class="flex-1 py-2 border border-dashed border-slate-300 rounded-lg text-xs text-slate-400 hover:border-blue-400 hover:text-blue-500 transition cursor-pointer" @click="addTextBlock(form.explanationBlocks)">+ Add text</button>
                        <button type="button" class="flex-1 py-2 border border-dashed border-amber-200 rounded-lg text-xs text-amber-400 hover:border-amber-400 hover:text-amber-500 transition cursor-pointer" @click="addImageBlock(form.explanationBlocks)">+ Add image</button>
                      </div>
                    </div>

                  </template>

                  <!-- ── Tab: Briefing ── -->
                  <template v-if="activeTab === 'briefing'">

                    <div class="space-y-1.5">
                      <label class="block text-sm font-semibold text-slate-700">
                        Briefing Content <span class="text-red-500">*</span>
                      </label>
                      <VueDraggable v-model="form.questionBlocks" :animation="150" handle=".drag-handle" class="space-y-2">
                        <div v-for="block in form.questionBlocks" :key="block._id" class="rounded-lg border border-slate-200 bg-white overflow-hidden">
                          <div class="flex items-center gap-1.5 px-2.5 py-1.5 bg-slate-50 border-b border-slate-100">
                            <div class="drag-handle cursor-grab text-slate-300 hover:text-slate-500 select-none text-base leading-none px-0.5" title="Drag to reorder">⠿</div>
                            <span class="text-xs font-semibold" :class="block.type === 'text' ? 'text-blue-600' : 'text-amber-600'">{{ block.type === 'text' ? 'Text' : 'Image' }}</span>
                            <div class="flex-1"></div>
                            <button type="button" class="text-slate-300 hover:text-red-500 transition text-xs leading-none cursor-pointer px-1" @click="removeBlock(form.questionBlocks, block._id)" title="Remove">✕</button>
                          </div>
                          <div v-if="block.type === 'text'" class="p-3 space-y-1.5">
                            <div class="flex items-center gap-0.5 px-1.5 py-1 bg-slate-50 border border-slate-200 rounded-lg w-fit">
                              <button type="button" class="px-1.5 py-0.5 text-xs font-bold text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(questionTextareas[block._id] ?? null, form.questionBlocks, block._id, '<b>', '</b>')">B</button>
                              <button type="button" class="px-1.5 py-0.5 text-xs italic text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(questionTextareas[block._id] ?? null, form.questionBlocks, block._id, '<i>', '</i>')">I</button>
                              <button type="button" class="px-1.5 py-0.5 text-xs underline text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(questionTextareas[block._id] ?? null, form.questionBlocks, block._id, '<u>', '</u>')">U</button>
                              <button type="button" class="px-1.5 py-0.5 text-xs line-through text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(questionTextareas[block._id] ?? null, form.questionBlocks, block._id, '<s>', '</s>')">S</button>
                              <div class="w-px h-3 bg-slate-200 mx-0.5"></div>
                              <button type="button" class="px-1.5 py-0.5 text-xs font-mono text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(questionTextareas[block._id] ?? null, form.questionBlocks, block._id, '<code>', '</code>')">&lt;/&gt;</button>
                              <div class="w-px h-3 bg-slate-200 mx-0.5"></div>
                              <button v-for="c in COLORS" :key="c.hex" type="button" :title="c.label" :style="{ backgroundColor: c.hex }" class="w-3.5 h-3.5 rounded-full cursor-pointer hover:scale-110 transition-transform flex-shrink-0" @click="applyBlockFormat(questionTextareas[block._id] ?? null, form.questionBlocks, block._id, colorTag(c.hex), '</span>')"></button>
                            </div>
                            <textarea
                              :ref="(el) => { questionTextareas[block._id] = el as HTMLTextAreaElement }"
                              v-model="block.content"
                              rows="2"
                              class="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 resize-none transition bg-white text-slate-900 placeholder-slate-400 font-[inherit]"
                              placeholder="Type here…"
                            ></textarea>
                          </div>
                          <div v-else class="p-3">
                            <div v-if="block.content" class="relative rounded-lg overflow-hidden">
                              <img :src="`/api/files/${block.content}`" class="max-h-48 w-full object-contain block rounded-lg bg-slate-100" alt="" />
                              <button type="button" class="absolute top-1.5 right-1.5 w-6 h-6 rounded-full bg-black/55 text-white border-none text-[0.7rem] cursor-pointer flex items-center justify-center leading-none hover:bg-red-600/90 transition-colors" @click="block.content = ''" title="Remove">✕</button>
                            </div>
                            <div v-else class="border-2 border-dashed rounded-xl p-5 flex items-center justify-center cursor-pointer transition select-none border-slate-200 bg-slate-50 hover:border-amber-400 hover:bg-amber-50" @click="uploadImageBlock(block)">
                              <div class="flex flex-col items-center gap-1 pointer-events-none">
                                <div class="text-3xl leading-none">🖼️</div>
                                <div class="text-sm font-semibold text-slate-600">Click to upload</div>
                                <div class="text-xs text-slate-400">or drag &amp; drop a file</div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </VueDraggable>
                      <div class="flex gap-2">
                        <button type="button" class="flex-1 py-2 border border-dashed border-slate-300 rounded-lg text-xs text-slate-400 hover:border-blue-400 hover:text-blue-500 transition cursor-pointer" @click="addTextBlock(form.questionBlocks)">+ Add text</button>
                        <button type="button" class="flex-1 py-2 border border-dashed border-amber-200 rounded-lg text-xs text-amber-400 hover:border-amber-400 hover:text-amber-500 transition cursor-pointer" @click="addImageBlock(form.questionBlocks)">+ Add image</button>
                      </div>
                    </div>

                  </template>

                </form>
              </div>
            </div>

            <!-- Preview panel -->
            <div class="w-1/2 flex flex-col overflow-hidden">

              <!-- Telegram top bar -->
              <div class="flex items-center gap-2 px-3 h-12 shrink-0" style="background: #2b5278">
                <div class="w-8 h-8 rounded-full flex items-center justify-center text-base shrink-0" style="background: #5288c1">🤖</div>
                <div class="flex-1 min-w-0">
                  <div class="text-white text-sm font-semibold leading-none mb-0.5 truncate">Quiz Bot</div>
                  <div class="text-[0.65rem]" style="color: #7fb3e8">bot</div>
                </div>
                <div class="text-white/40 text-base leading-none select-none">⋮</div>
              </div>

              <!-- Chat body -->
              <div class="flex-1 overflow-y-auto px-2 pt-2 pb-3 flex flex-col gap-1" style="background: #e8edf2">

                <!-- Date chip -->
                <div class="flex justify-center mb-1.5">
                  <span class="tg-date-chip">Today</span>
                </div>

                <!-- ── Briefing preview ── -->
                <template v-if="form.isBriefing">
                  <template v-for="block in form.questionBlocks" :key="'pb'+block._id">
                    <div v-if="block.type === 'text' && block.content.trim()" class="tg-row">
                      <div class="tg-avatar">🤖</div>
                      <div class="tg-msg"><span v-html="tgToHtml(block.content)"></span><span class="tg-ts">12:00</span></div>
                    </div>
                    <div v-else-if="block.type === 'image' && block.content" class="tg-row">
                      <div class="tg-avatar">🤖</div>
                      <div class="tg-msg tg-img-msg"><img :src="`/api/files/${block.content}`" class="tg-qimg" alt="" /></div>
                    </div>
                  </template>
                  <div v-if="!form.questionBlocks.some(b => b.content.trim())" class="tg-row">
                    <div class="tg-avatar">🤖</div>
                    <div class="tg-msg"><span class="text-slate-400 italic">Your instructions…</span><span class="tg-ts">12:00</span></div>
                  </div>
                  <div class="tg-keyboard">
                    <div class="tg-kb-btn tg-kb-ready">▶️ Press READY to begin</div>
                  </div>
                </template>

                <!-- ── Question preview (Multiple Choice / Photo) ── -->
                <template v-else>

                <!-- Question blocks in order -->
                <template v-if="form.questionBlocks.some(b => b.content.trim())">
                  <template v-for="block in form.questionBlocks" :key="'pq'+block._id">
                    <div v-if="block.type === 'text' && block.content.trim()" class="tg-row">
                      <div class="tg-avatar">🤖</div>
                      <div v-if="block._id === lastFilledQTextId" class="tg-msg tg-poll">
                        <div class="tg-poll-q"><span v-html="tgToHtml(block.content)"></span><span class="tg-ts">12:00</span></div>
                      </div>
                      <div v-else class="tg-msg"><span v-html="tgToHtml(block.content)"></span><span class="tg-ts">12:00</span></div>
                    </div>
                    <div v-else-if="block.type === 'image' && block.content" class="tg-row">
                      <div class="tg-avatar">🤖</div>
                      <div class="tg-msg tg-img-msg"><img :src="`/api/files/${block.content}`" class="tg-qimg" alt="" /></div>
                    </div>
                  </template>
                </template>
                <template v-else>
                  <div class="tg-row">
                    <div class="tg-avatar">🤖</div>
                    <div class="tg-msg tg-poll">
                      <div class="tg-poll-q"><span class="text-slate-400 italic">Your question…</span><span class="tg-ts">12:00</span></div>
                    </div>
                  </div>
                </template>

                <!-- Inline keyboard quick replies -->
                <template v-if="!form.expectPhoto && filledOptions.length">
                  <div class="tg-keyboard">
                    <div v-for="(opt, i) in filledOptions" :key="'kb'+i" class="tg-kb-btn">
                      {{ String.fromCharCode(65 + i) }} · {{ opt }}
                    </div>
                    <div v-if="hasFilledHintBlocks" class="tg-kb-btn tg-kb-hint">💡 Hint</div>
                  </div>
                </template>

                <!-- After-hint service + hint blocks -->
                <template v-if="hasFilledHintBlocks">
                  <div class="flex justify-center my-1.5">
                    <span class="tg-service-msg">after hint button click</span>
                  </div>
                  <template v-for="block in form.hintBlocks" :key="'ph'+block._id">
                    <div v-if="block.type === 'text' && block.content.trim()" class="tg-row">
                      <div class="tg-avatar">🤖</div>
                      <div class="tg-msg tg-hint-msg">💡 <span v-html="tgToHtml(block.content)"></span><span class="tg-ts">12:01</span></div>
                    </div>
                    <div v-else-if="block.type === 'image' && block.content" class="tg-row">
                      <div class="tg-avatar">🤖</div>
                      <div class="tg-msg tg-img-msg"><img :src="`/api/files/${block.content}`" class="tg-qimg" alt="" /></div>
                    </div>
                  </template>
                </template>

                <!-- After-answer service message -->
                <div class="flex justify-center my-1.5">
                  <span class="tg-service-msg">after answer</span>
                </div>

                </template> <!-- end v-else question preview -->

                <!-- After Answer blocks (not shown for briefing) -->
                <template v-if="!form.isBriefing && hasFilledExplanationBlocks">
                  <template v-for="block in form.explanationBlocks" :key="'pe'+block._id">
                    <div v-if="block.type === 'text' && block.content.trim()" class="tg-row">
                      <div class="tg-avatar">🤖</div>
                      <div class="tg-msg tg-explanation-msg"><span v-html="tgToHtml(block.content)"></span><span class="tg-ts">12:02</span></div>
                    </div>
                    <div v-else-if="block.type === 'image' && block.content" class="tg-row">
                      <div class="tg-avatar">🤖</div>
                      <div class="tg-msg tg-img-msg"><img :src="`/api/files/${block.content}`" class="tg-qimg" alt="" /></div>
                    </div>
                  </template>
                </template>
                <div v-else-if="!form.isBriefing" class="tg-empty-explanation">No after answer content added yet…</div>

              </div>
            </div>
          </div>

          <!-- Footer -->
          <div class="flex items-center justify-end gap-2 px-6 py-4 border-t border-slate-100 bg-slate-50/60">
            <span v-if="error" class="text-red-500 text-xs mr-auto">{{ error }}</span>
            <button
              type="button"
              class="px-4 py-2 rounded-xl border border-slate-200 text-sm font-medium text-slate-600 hover:bg-slate-100 transition cursor-pointer"
              @click="$emit('close')"
            >Cancel</button>
            <button
              type="button"
              class="px-5 py-2 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold transition disabled:opacity-60 cursor-pointer disabled:cursor-not-allowed inline-flex items-center gap-2"
              :disabled="saving"
              @click="submit"
            >
              <span v-if="saving" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin inline-block"></span>
              {{ isEdit ? 'Save changes' : 'Create' }}
            </button>
          </div>

        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup lang="ts">
import { ref, watch, computed, nextTick } from 'vue'
import { VueDraggable } from 'vue-draggable-plus'
import type { Question, QuestionRequest, ContentBlock } from '@/types/question'
import type { Folder } from '@/types/folder'
import { questionsService } from '@/services/questionsService'

// ── FormBlock (internal form type with stable drag key) ──────────────────
interface FormBlock {
  _id: string
  type: 'text' | 'image'
  content: string
}

function makeId(): string {
  return Math.random().toString(36).slice(2) + Date.now().toString(36)
}

function makeBlock(type: 'text' | 'image', content = ''): FormBlock {
  return { _id: makeId(), type, content }
}

// ── Props / emits ──────────────────────────────────────────────────────────
const props = defineProps<{
  visible: boolean
  question: Question | null
  folders: Folder[]
  defaultFolderId?: number | null
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'saved', q: Question): void
}>()



// ── Form state ─────────────────────────────────────────────────────────────
function blankForm() {
  return {
    questionBlocks: [makeBlock('text')] as FormBlock[],
    options: [] as string[],
    answer: '',
    mark: null as number | null,
    expectPhoto: false,
    isBriefing: false,
    hintBlocks: [] as FormBlock[],
    explanationBlocks: [] as FormBlock[],
    folderId: null as number | null,
  }
}

const form = ref(blankForm())
const saving = ref(false)
const error = ref('')
const isEdit = ref(false)
const activeTab = ref<'question' | 'answer' | 'mark' | 'hint' | 'explanation' | 'briefing'>('question')
const tabs = computed(() => {
  if (form.value.isBriefing) {
    return [{ value: 'briefing' as const, label: 'Briefing' }]
  }
  if (form.value.expectPhoto) {
    return [
      { value: 'question' as const, label: 'Question' },
      { value: 'mark' as const, label: 'Mark' },
      { value: 'hint' as const, label: 'Hint' },
      { value: 'explanation' as const, label: 'After Answer' },
    ]
  }
  return [
    { value: 'question' as const, label: 'Question' },
    { value: 'answer' as const, label: 'Answer' },
    { value: 'mark' as const, label: 'Mark' },
    { value: 'hint' as const, label: 'Hint' },
    { value: 'explanation' as const, label: 'After Answer' },
  ]
})
const filledOptions = computed(() => form.value.options.filter(o => o.trim()))
const hasFilledHintBlocks = computed(() => form.value.hintBlocks.some(b => b.content.trim()))
const hasFilledExplanationBlocks = computed(() => form.value.explanationBlocks.some(b => b.content.trim()))
const lastFilledQTextId = computed(() => {
  const filled = form.value.questionBlocks.filter(b => b.type === 'text' && b.content.trim())
  return filled.length ? filled[filled.length - 1]._id : null
})

// ── Rich text formatting ───────────────────────────────────────────────────
const questionTextareas = ref<Record<string, HTMLTextAreaElement | null>>({})
const hintTextareas = ref<Record<string, HTMLTextAreaElement | null>>({})
const explanationTextareas = ref<Record<string, HTMLTextAreaElement | null>>({})

const COLORS = [
  { label: 'Red',    hex: '#e53935' },
  { label: 'Orange', hex: '#fb8c00' },
  { label: 'Yellow', hex: '#fdd835' },
  { label: 'Green',  hex: '#43a047' },
  { label: 'Cyan',   hex: '#00acc1' },
  { label: 'Blue',   hex: '#1e88e5' },
  { label: 'Purple', hex: '#8e24aa' },
]

function colorTag(hex: string): string {
  return `<span style="color: ${hex}">`
}

// Shared smart toggle: handles both plain tags and color spans
function toggleFormat(text: string, start: number, end: number, open: string, close: string): { text: string, selStart: number, selEnd: number } {
  const isColor = open.startsWith('<span style="color:')
  if (isColor) {
    if (text.slice(end, end + close.length) === close) {
      const before = text.slice(0, start)
      const match = before.match(/<span style="color: [^"]+">$/)
      if (match) {
        const existingOpen = match[0]
        const preLen = existingOpen.length
        if (existingOpen === open) {
          const newText = text.slice(0, start - preLen) + text.slice(start, end) + text.slice(end + close.length)
          return { text: newText, selStart: start - preLen, selEnd: end - preLen }
        } else {
          const newText = text.slice(0, start - preLen) + open + text.slice(start, end) + close + text.slice(end + close.length)
          return { text: newText, selStart: start - preLen + open.length, selEnd: end - preLen + open.length }
        }
      }
    }
    const newText = text.slice(0, start) + open + text.slice(start, end) + close + text.slice(end)
    return { text: newText, selStart: start + open.length, selEnd: end + open.length }
  }
  if (start >= open.length && text.slice(start - open.length, start) === open && text.slice(end, end + close.length) === close) {
    const newText = text.slice(0, start - open.length) + text.slice(start, end) + text.slice(end + close.length)
    return { text: newText, selStart: start - open.length, selEnd: end - open.length }
  }
  const newText = text.slice(0, start) + open + text.slice(start, end) + close + text.slice(end)
  return { text: newText, selStart: start + open.length, selEnd: end + open.length }
}

function applyBlockFormat(
  el: HTMLTextAreaElement | null,
  blocks: FormBlock[],
  blockId: string,
  open: string,
  close: string
) {
  if (!el) return
  const block = blocks.find(b => b._id === blockId)
  if (!block) return
  const start = el.selectionStart ?? 0
  const end = el.selectionEnd ?? 0
  const result = toggleFormat(block.content, start, end, open, close)
  block.content = result.text
  nextTick(() => { el.focus(); el.setSelectionRange(result.selStart, result.selEnd) })
}

function tgToHtml(text: string): string {
  if (!text) return ''
  return text.replace(/\n/g, '<br>')
}

function setQuestionType(expectPhoto: boolean, isBriefing: boolean) {
  form.value.expectPhoto = expectPhoto
  form.value.isBriefing = isBriefing
  activeTab.value = isBriefing ? 'briefing' : 'question'
  if (activeTab.value === 'answer' && (expectPhoto || isBriefing)) {
    activeTab.value = 'question'
  }
}

watch(() => props.visible, (v) => {
  if (!v) return
  error.value = ''
  if (props.question) {
    const q = props.question
    activeTab.value = q.isBriefing ? 'briefing' : 'question'
    isEdit.value = true
    form.value = {
      questionBlocks: q.questionBlocks.length
        ? q.questionBlocks.map(b => ({ ...b, _id: makeId() }))
        : [makeBlock('text')],
      options: [...q.options],
      answer: q.answer ?? '',
      mark: q.mark ?? null,
      expectPhoto: q.expectPhoto,
      isBriefing: q.isBriefing,
      hintBlocks: q.hintBlocks.map(b => ({ ...b, _id: makeId() })),
      explanationBlocks: q.explanationBlocks.map(b => ({ ...b, _id: makeId() })),
      folderId: q.folderId ?? null,
    }
  } else {
    isEdit.value = false
    activeTab.value = 'question'
    form.value = { ...blankForm(), folderId: props.defaultFolderId ?? null }
  }
})

// ── Block helpers ──────────────────────────────────────────────────────────
function addTextBlock(list: FormBlock[]) { list.push(makeBlock('text')) }
function addImageBlock(list: FormBlock[]) { list.push(makeBlock('image')) }
function removeBlock(list: FormBlock[], id: string) {
  const idx = list.findIndex(b => b._id === id)
  if (idx !== -1) list.splice(idx, 1)
}

async function uploadImageBlock(block: FormBlock) {
  await new Promise<void>((resolve) => {
    const input = document.createElement('input')
    input.type = 'file'
    input.accept = 'image/*'
    input.onchange = async () => {
      const file = input.files?.[0]
      if (file) {
        try {
          block.content = await questionsService.uploadFile(file)
        } catch {
          error.value = 'Image upload failed.'
        }
      }
      resolve()
    }
    input.click()
  })
}

// ── Options helpers ────────────────────────────────────────────────────────
function addOption() { form.value.options.push('') }
function removeOption(i: number) {
  const removed = form.value.options.splice(i, 1)[0]
  if (form.value.answer === removed) form.value.answer = ''
}

async function submit() {
  if (!form.value.questionBlocks.some(b => b.type === 'text' && b.content.trim())) {
    error.value = 'At least one text block is required.'
    return
  }
  error.value = ''
  saving.value = true

  function toContentBlocks(list: FormBlock[]): ContentBlock[] {
    return list
      .filter(b => b.type === 'image' ? b.content.trim() : b.content.trim())
      .map(({ type, content }) => ({ type, content }))
  }

  const payload: QuestionRequest = {
    questionBlocks: toContentBlocks(form.value.questionBlocks),
    options: (form.value.expectPhoto || form.value.isBriefing) ? [] : form.value.options.filter(o => o.trim()),
    answer: form.value.isBriefing ? null : (form.value.answer || null),
    mark: form.value.isBriefing ? null : (form.value.mark ?? null),
    expectPhoto: form.value.expectPhoto,
    isBriefing: form.value.isBriefing,
    hintBlocks: toContentBlocks(form.value.hintBlocks),
    explanationBlocks: toContentBlocks(form.value.explanationBlocks),
    folderId: form.value.folderId ?? null,
  }

  try {
    const saved = isEdit.value && props.question
      ? await questionsService.update(props.question.id, payload)
      : await questionsService.create(payload)
    emit('saved', saved)
  } catch {
    error.value = 'Failed to save question. Please try again.'
  } finally {
    saving.value = false
  }
}

</script>

<style scoped>
/* ── Date / service chips ── */
.tg-date-chip, .tg-service-msg {
  font-size: 0.6rem;
  color: rgba(255,255,255,0.9);
  background: rgba(0,0,0,0.22);
  border-radius: 10px;
  padding: 0.2rem 0.6rem;
  white-space: nowrap;
}
.tg-service-msg { font-style: italic; }

/* ── Message row ── */
.tg-row {
  display: flex;
  align-items: flex-end;
  gap: 4px;
}
.tg-avatar {
  width: 26px;
  height: 26px;
  min-width: 26px;
  border-radius: 50%;
  background: #5288c1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  flex-shrink: 0;
}

.tg-msg {
  background: #fff;
  border-radius: 2px 12px 12px 12px;
  padding: 0.4rem 0.55rem 0.3rem;
  font-size: 0.8rem;
  color: #111;
  box-shadow: 0 1px 2px rgba(0,0,0,0.08);
  word-break: break-word;
  line-height: 1.45;
  position: relative;
  max-width: 82%;
  min-width: 0;
  overflow: hidden;
}

/* Timestamp ── floated to bottom-right inside bubble */
.tg-ts {
  font-size: 0.6rem;
  color: #8d9eac;
  float: right;
  margin-left: 5px;
  margin-top: 3px;
  line-height: 1;
  display: inline-block;
}

.tg-intro-blue { }

.tg-img-msg { padding: 0.25rem; max-width: 240px; }
.tg-qimg {
  width: 100%;
  border-radius: 10px;
  display: block;
  max-height: 180px;
  object-fit: cover;
}

.tg-poll { padding: 0.55rem 0.65rem 0.4rem; min-width: 175px; }

.tg-poll-number {
  font-size: 0.72rem;
  font-weight: 700;
  color: #111;
  margin-bottom: 0.3rem;
}

.tg-poll-header {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  margin-bottom: 0.5rem;
}
.tg-poll-icon { font-size: 0.85rem; line-height: 1; }
.tg-poll-type {
  font-size: 0.6rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: #4fabe2;
}

.tg-poll-q {
  font-size: 0.875rem;
  font-weight: normal;
  color: #111;
  margin-bottom: 0.45rem;
  line-height: 1.35;
}

.tg-photo-note {
  font-size: 0.8125rem;
  color: #6b7280;
  font-style: italic;
  padding: 0.5rem 0;
}

.tg-empty-opts {
  font-size: 0.8rem;
  color: #9ca3af;
  font-style: italic;
  padding: 0.25rem 0 0.5rem;
}

.tg-opt { margin-bottom: 0.375rem; }
.tg-opt-row {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  font-size: 0.875rem;
  margin-bottom: 0.2rem;
}
.tg-opt-letter {
  font-size: 0.62rem;
  font-weight: 700;
  color: #4fabe2;
  background: #e8f4fd;
  border-radius: 50%;
  width: 15px;
  height: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.tg-opt-text { flex: 1; color: #111; font-size: 0.78rem; }
.tg-opt-pct { font-size: 0.62rem; color: #8d9eac; flex-shrink: 0; min-width: 22px; text-align: right; }
.tg-opt-bar { height: 2px; background: #e8edf2; border-radius: 1px; overflow: hidden; }
.tg-opt-fill { height: 100%; width: 0%; background: #4fabe2; border-radius: 1px; }

.tg-poll-meta {
  font-size: 0.62rem;
  color: #8d9eac;
  margin-top: 0.35rem;
  padding-top: 0.35rem;
  border-top: 1px solid #f0f4f8;
}

.tg-vote-btn {
  display: none; /* replaced by inline keyboard */
}

.tg-keyboard {
  display: flex;
  flex-direction: column;
  gap: 3px;
  margin-top: 1px;
  padding-left: 30px;
  max-width: 90%;
}

.tg-kb-btn {
  background: rgba(255,255,255,0.92);
  border-radius: 8px;
  padding: 0.35rem 0.6rem;
  font-size: 0.78rem;
  color: #2196f3;
  font-weight: 500;
  text-align: center;
  box-shadow: 0 1px 2px rgba(0,0,0,0.07);
  cursor: default;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tg-kb-hint { color: #f59e0b; }
.tg-kb-ready { color: #16a34a; font-weight: 700; }
.tg-hint-msg { color: #374151; }
.tg-explanation-msg { color: #111; }

.tg-empty-explanation {
  font-size: 0.72rem;
  color: #9ca3af;
  font-style: italic;
  text-align: center;
  padding: 0.5rem 0;
}
</style>
