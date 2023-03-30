package com.example.notenexus.Fragments

import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.notenexus.Models.NoteModel
import com.example.notenexus.R
import com.example.notenexus.Utils.hideKeyboard
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.transition.MaterialContainerTransform
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.thebluealliance.spectrum.SpectrumPalette
import com.yahiaangelo.markdownedittext.MarkdownEditText
import com.yahiaangelo.markdownedittext.MarkdownStylesBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


class SaveEditFragment : Fragment() {

    lateinit var noteContentFragmentParent: RelativeLayout
    lateinit var toolbarFragmentNoteContent: RelativeLayout

    private lateinit var navController: NavController
    private val data=NoteModel()
    private var note: NoteModel?=null

    lateinit var lastEdited:TextView
    lateinit var saveNote:ImageView
    lateinit var etTitle:EditText
    lateinit var etNoteContent:MarkdownEditText

    lateinit var fabColorPick: FloatingActionButton
    lateinit var bottomBar: LinearLayout
    lateinit var styleBar:MarkdownStylesBar
    private var  color:Int=-1315861
    private var noteId:String?=null
    private var mContext: Context?=null



    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val animation=MaterialContainerTransform().apply{

            drawingViewId=R.id.fragments
            scrimColor=R.color.transparent
            duration=300L
        }

        sharedElementEnterTransition=animation
        sharedElementReturnTransition=animation

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val callBack=object:OnBackPressedCallback(true){
            override fun handleOnBackPressed() {

                if(etTitle.text.toString().isEmpty() && etNoteContent.text.toString().isEmpty() && noteId==null){
                    Navigation.findNavController(view!!).navigate(R.id.action_saveEditFragment_to_noteFragment)
                }
                else{
                    saveNote()
                }

            }

        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,callBack)


        return inflater.inflate(R.layout.fragment_save_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.apply{
            noteContentFragmentParent=findViewById(R.id.noteContentFragmentParent)
            toolbarFragmentNoteContent=findViewById(R.id.toolbarFragmentNoteContent)
            navController=Navigation.findNavController(this)

            lastEdited=findViewById(R.id.last_edited)
            saveNote=findViewById(R.id.save_note)
            etTitle=findViewById(R.id.et_title)
            etNoteContent=findViewById(R.id.et_noteContent)
            fabColorPick=findViewById(R.id.fab_color_pick)
            bottomBar=findViewById(R.id.bottom_bar)
            styleBar=findViewById(R.id.style_bar)

        }

        noteId=arguments?.getString("noteId")

        CoroutineScope(Dispatchers.Main).launch {
            delay(10)
            ViewCompat.setTransitionName(
                noteContentFragmentParent,
                "recyclerView_${note?.id}"
            )

        }

        requireView().hideKeyboard()
        setUpNote()

        lastEdited.text=getString(R.string.edited_on,SimpleDateFormat.getInstance().format(Date()))

        try{

            etNoteContent.setOnFocusChangeListener { _, hasFocus ->
                if(hasFocus){
                    bottomBar.visibility=View.VISIBLE

                    etNoteContent.setStylesBar(styleBar)

                }
                else{
                    bottomBar.visibility=View.GONE
                }
            }

        }
        catch(e:Throwable){
            Log.d("TAG",e.stackTraceToString())
        }

        fabColorPick.setOnClickListener {

            val bottomSheetDialog=BottomSheetDialog(
                requireContext(),
                R.style.BottomSheetDialogTheme
            )

            val bottomSheetView:View=layoutInflater.inflate(
                R.layout.bottom_sheet_layout,
                null
            )
            with(
                bottomSheetDialog
            ){
                setContentView(bottomSheetView)
                show()
            }

            val colorPicker=bottomSheetDialog.findViewById<SpectrumPalette>(R.id.colorPicker)
            val bottomSheetParent=bottomSheetDialog.findViewById<CardView>(R.id.bottomSheetParent)

            colorPicker?.apply {

                setSelectedColor(color)
                bottomSheetParent?.setCardBackgroundColor(color)

                setOnColorSelectedListener {
                    value->
                    color=value

                    noteContentFragmentParent.setBackgroundColor(color)
                    toolbarFragmentNoteContent.setBackgroundColor(color)
                    bottomBar.setBackgroundColor(color)

                    activity?.window?.statusBarColor=color
                    activity?.window?.navigationBarColor=color

                    bottomSheetParent?.setCardBackgroundColor(color)

                }

            }

            bottomSheetView.post {
                bottomSheetDialog.behavior.state=BottomSheetBehavior.STATE_EXPANDED
            }

        }

        saveNote.setOnClickListener {
            saveNote()
        }

    }

    private fun setUpNote() {
        noteId=arguments?.getString("noteId")
        if(noteId==null){
            lastEdited.text=getString(R.string.edited_on,SimpleDateFormat
                .getInstance().format(Date()))

        }
        if(noteId!=null){

            FirebaseFirestore.getInstance().collection("notes")
                .document(FirebaseAuth.getInstance().uid.toString())
                .collection("myNotes").document(noteId!!).addSnapshotListener { value, error ->
                    var data=value?.toObject(NoteModel::class.java)

                    if(data!=null){
                        etTitle.setText(data.title)
                        etNoteContent.renderMD(data.content)
                        lastEdited.text=getString(R.string.edited_on,SimpleDateFormat
                            .getInstance().format(Date(data.date)))

                        color=data.color

                        noteContentFragmentParent.setBackgroundColor(color)
                        toolbarFragmentNoteContent.setBackgroundColor(color)
                        bottomBar.setBackgroundColor(color)

                        activity?.window?.statusBarColor=color
                        activity?.window?.navigationBarColor=color

                    }
                }

        }
    }

    private fun saveNote(){

        if(etTitle.text.toString().isEmpty() && etNoteContent.text.toString().isEmpty()){
            Toast.makeText(requireContext(),"Something is Empty",Toast.LENGTH_SHORT).show()
        }
        else if(etTitle.text.toString().isEmpty()){
            etTitle.setText("Untitled Note")

            val documentReference:DocumentReference=FirebaseFirestore.getInstance().collection("notes")
                .document(FirebaseAuth.getInstance().uid.toString()).collection("myNotes").document()

            data.id=documentReference.id
            data.title=etTitle.text.toString()
            data.content=etNoteContent.getMD()
            data.date=Date().time
            data.color=color

            when(noteId){
                null->{
                    documentReference.set(data).addOnSuccessListener {
                        Toast.makeText(requireContext(),"Note Saved",Toast.LENGTH_SHORT).show()
                        navController.navigate(R.id.action_saveEditFragment_to_noteFragment)
                    }
                }
                else->{
                    updateNote()
                }
            }

        }

        else{
            val documentReference:DocumentReference=FirebaseFirestore.getInstance().collection("notes")
                .document(FirebaseAuth.getInstance().uid.toString()).collection("myNotes").document()

            data.id=documentReference.id
            data.title=etTitle.text.toString()
            data.content=etNoteContent.getMD()
            data.date=Date().time
            data.color=color

            when(noteId){
                null->{
                    documentReference.set(data).addOnSuccessListener {
                        Toast.makeText(requireContext(),"Note Saved",Toast.LENGTH_SHORT).show()
                        navController.navigate(R.id.action_saveEditFragment_to_noteFragment)
                    }
                }
                else->{
                    updateNote()
                }
            }
        }

    }

    private fun updateNote() {
        data.id=noteId!!
        data.title=etTitle.text.toString()
        data.content=etNoteContent.getMD()
        data.date=Date().time
        data.color=color

        FirebaseFirestore.getInstance().collection("notes")
            .document(FirebaseAuth.getInstance().uid.toString()).collection("myNotes").document(noteId!!).set(data).addOnSuccessListener {
                navController=Navigation.findNavController(requireView())
                navController.navigate(R.id.action_saveEditFragment_to_noteFragment)
            }

    }


}