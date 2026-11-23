package com.example.beetles.ui

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.example.beetles.R
import com.example.beetles.data.BeetleSkinStore
import com.example.beetles.databinding.FragmentSkinsBinding
import com.example.beetles.databinding.ItemBeetleModelBinding
import com.example.beetles.databinding.ItemSkinBinding
import com.example.beetles.game.InsectKind
import com.example.beetles.model.BeetleModel
import com.example.beetles.model.BeetleMood
import com.example.beetles.model.BeetleSkinCatalog
import com.example.beetles.model.BeetleSkinSet
import com.example.beetles.model.KindLook
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class SkinsFragment : Fragment() {

    private var _binding: FragmentSkinsBinding? = null
    private val binding get() = _binding!!

    private val store: BeetleSkinStore by inject()
    private val gameViewModel: GameViewModel by activityViewModel()

    private lateinit var catalog: BeetleSkinCatalog
    private var draft: BeetleSkinSet = BeetleSkinSet.DEFAULT
    private var editingKind: InsectKind = InsectKind.NORMAL
    private var suppressCallbacks = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSkinsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        catalog = store.loadCatalog()
        draft = catalog.selected()
        setupControls()
        bindDraftToUi()
        renderSavedList()
        refreshActiveLabel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupControls() {
        binding.sbSize.max = BeetleSkinSet.SIZE_STEPS
        binding.sbSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser || suppressCallbacks) return
                updateLook { it.copy(sizeScale = BeetleSkinSet.sizeFromProgress(progress)) }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        binding.rgKind.setOnCheckedChangeListener { _: RadioGroup, checkedId: Int ->
            if (suppressCallbacks) return@setOnCheckedChangeListener
            editingKind = kindFromId(checkedId)
            bindKindEditor()
        }
        binding.rgMood.setOnCheckedChangeListener { _: RadioGroup, checkedId: Int ->
            if (suppressCallbacks) return@setOnCheckedChangeListener
            updateLook {
                it.copy(mood = if (checkedId == R.id.rbMoodEvil) BeetleMood.EVIL else BeetleMood.KIND)
            }
        }
        binding.etSkinName.doAfterTextChanged { text ->
            if (suppressCallbacks) return@doAfterTextChanged
            draft = draft.copy(name = text?.toString().orEmpty())
        }
        binding.btnSaveSkin.setOnClickListener { saveDraft() }
        binding.btnDeleteSkin.setOnClickListener { deleteCurrent() }
    }

    private fun bindDraftToUi() {
        suppressCallbacks = true
        binding.etSkinName.setText(draft.name)
        binding.rgKind.check(idFromKind(editingKind))
        suppressCallbacks = false
        bindKindEditor()
        binding.btnDeleteSkin.isEnabled = !draft.builtIn
        updateSetPreview()
    }

    private fun bindKindEditor() {
        val look = currentLook()
        suppressCallbacks = true
        binding.sbSize.progress = BeetleSkinSet.sizeProgress(look.sizeScale)
        binding.rgMood.check(
            if (look.mood == BeetleMood.EVIL) R.id.rbMoodEvil else R.id.rbMoodKind
        )
        suppressCallbacks = false
        renderModels()
        renderColorRow(binding.llBodyColors, look.bodyColor) { color ->
            updateLook { it.copy(bodyColor = color) }
            bindKindEditor()
        }
        renderColorRow(binding.llAccentColors, look.accentColor) { color ->
            updateLook { it.copy(accentColor = color) }
            bindKindEditor()
        }
        renderColorRow(binding.llEyeColors, look.eyeColor) { color ->
            updateLook { it.copy(eyeColor = color) }
            bindKindEditor()
        }
        updatePreview()
    }

    private fun renderModels() {
        binding.llModels.removeAllViews()
        val selected = currentLook().model
        val margin = resources.getDimensionPixelSize(R.dimen.skin_swatch_margin)
        BeetleModel.forKind(editingKind).forEach { model ->
            val item = ItemBeetleModelBinding.inflate(layoutInflater, binding.llModels, false)
            item.tvModelName.text = model.title
            item.previewModel.look = model.defaultLook()
            val params = (item.root.layoutParams as LinearLayout.LayoutParams)
            params.marginEnd = margin
            item.root.layoutParams = params
            item.root.background = modelCardBackground(model == selected)
            item.root.setOnClickListener { selectModel(model) }
            binding.llModels.addView(item.root)
        }
    }

    private fun selectModel(model: BeetleModel) {
        val current = currentLook()
        updateLook {
            model.defaultLook().copy(
                sizeScale = current.sizeScale,
                mood = current.mood
            )
        }
        bindKindEditor()
    }

    private fun renderColorRow(
        row: LinearLayout,
        selected: Int,
        onPick: (Int) -> Unit
    ) {
        row.removeAllViews()
        val size = resources.getDimensionPixelSize(R.dimen.skin_swatch_size)
        val margin = resources.getDimensionPixelSize(R.dimen.skin_swatch_margin)
        PALETTE.forEach { color ->
            val swatch = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    marginEnd = margin
                }
                background = swatchDrawable(color, selected = color == selected)
                setOnClickListener { onPick(color) }
            }
            row.addView(swatch)
        }
    }

    private fun updateLook(transform: (KindLook) -> KindLook) {
        draft = draft.withLook(editingKind, transform(currentLook()))
        updatePreview()
        updateSetPreview()
    }

    private fun updatePreview() {
        val look = currentLook()
        binding.previewBeetle.look = look
        binding.tvSizeValue.text = getString(
            R.string.skins_size_value,
            (look.sizeScale * 100).toInt()
        )
    }

    private fun updateSetPreview() {
        binding.previewSet.skinSet = draft
    }

    private fun currentLook(): KindLook = draft.look(editingKind)

    private fun saveDraft() {
        when (val result = catalog.save(draft.copy(name = binding.etSkinName.text?.toString().orEmpty()))) {
            BeetleSkinCatalog.SaveResult.EmptyName -> {
                Toast.makeText(requireContext(), R.string.skins_error_empty_name, Toast.LENGTH_SHORT).show()
            }
            BeetleSkinCatalog.SaveResult.NameTaken -> {
                Toast.makeText(requireContext(), R.string.skins_error_name_taken, Toast.LENGTH_SHORT).show()
            }
            is BeetleSkinCatalog.SaveResult.Ok -> {
                draft = result.set
                persistAndRefresh()
                bindDraftToUi()
                Toast.makeText(requireContext(), R.string.skins_saved, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteCurrent() {
        if (!catalog.delete(draft.id)) {
            Toast.makeText(requireContext(), R.string.skins_error_delete, Toast.LENGTH_SHORT).show()
            return
        }
        draft = catalog.selected()
        persistAndRefresh()
        bindDraftToUi()
        Toast.makeText(requireContext(), R.string.skins_deleted, Toast.LENGTH_SHORT).show()
    }

    private fun persistAndRefresh() {
        store.saveCatalog(catalog)
        gameViewModel.refreshSkin()
        renderSavedList()
        refreshActiveLabel()
    }

    private fun renderSavedList() {
        binding.llSavedSkins.removeAllViews()
        catalog.list().forEach { set ->
            val item = ItemSkinBinding.inflate(layoutInflater, binding.llSavedSkins, false)
            item.tvSkinName.text = if (set.id == catalog.selectedId) {
                getString(R.string.skins_item_selected, set.name)
            } else {
                set.name
            }
            item.tvSkinMeta.text = getString(
                R.string.skins_item_meta,
                set.normal.model.title,
                set.fast.model.title,
                set.rare.model.title,
                set.golden.model.title
            )
            item.previewSet.skinSet = set
            item.root.setOnClickListener {
                catalog.select(set.id)
                draft = set
                persistAndRefresh()
                bindDraftToUi()
            }
            binding.llSavedSkins.addView(item.root)
        }
    }

    private fun refreshActiveLabel() {
        binding.tvActiveSkin.text = getString(R.string.skins_active, catalog.selected().name)
    }

    private fun swatchDrawable(color: Int, selected: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
            setStroke(
                if (selected) resources.getDimensionPixelSize(R.dimen.skin_swatch_stroke) else 0,
                0xFF212121.toInt()
            )
        }
    }

    private fun modelCardBackground(selected: Boolean) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = resources.displayMetrics.density * 12f
        setColor(0x00000000)
        setStroke(
            resources.getDimensionPixelSize(R.dimen.skin_swatch_stroke),
            if (selected) {
                ContextCompat.getColor(requireContext(), R.color.purple_700)
            } else {
                0x00000000
            }
        )
    }

    private fun kindFromId(id: Int): InsectKind = when (id) {
        R.id.rbKindFast -> InsectKind.FAST
        R.id.rbKindRare -> InsectKind.RARE
        R.id.rbKindGolden -> InsectKind.GOLDEN
        else -> InsectKind.NORMAL
    }

    private fun idFromKind(kind: InsectKind): Int = when (kind) {
        InsectKind.NORMAL -> R.id.rbKindNormal
        InsectKind.FAST -> R.id.rbKindFast
        InsectKind.RARE -> R.id.rbKindRare
        InsectKind.GOLDEN -> R.id.rbKindGolden
    }

    private companion object {
        val PALETTE = intArrayOf(
            0xFF6D4C41.toInt(),
            0xFF3E2723.toInt(),
            0xFF212121.toInt(),
            0xFFB71C1C.toInt(),
            0xFFC62828.toInt(),
            0xFFE91E63.toInt(),
            0xFF6A1B9A.toInt(),
            0xFF1565C0.toInt(),
            0xFF00838F.toInt(),
            0xFF2E7D32.toInt(),
            0xFF33691E.toInt(),
            0xFFF9A825.toInt(),
            0xFFFFD54F.toInt(),
            0xFFFF6F00.toInt(),
            0xFF90A4AE.toInt(),
            0xFFECEFF1.toInt()
        )
    }
}
