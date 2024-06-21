package com.enesk.yemekkitabi

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.enesk.yemekkitabi.databinding.FragmentListeBinding
import com.enesk.yemekkitabi.databinding.FragmentTarifBinding
import com.google.android.material.snackbar.Snackbar
import java.io.IOException


class TarifFragment : Fragment() {

    private var _binding: FragmentTarifBinding? = null
    private val binding get() = _binding!!
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    private var secilenGorsel : Uri? = null //görsel konumu için
    private var secilenBitMap : Bitmap? = null //bitmape çevirmemiz için

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTarifBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.silButton.setOnClickListener{sil(it)}
        binding.kaydetButton.setOnClickListener { kaydet(it) }
        binding.imageView.setOnClickListener{gorselSec(it)}

        arguments?.let {
            val bilgi = TarifFragmentArgs.fromBundle(it).bilgi

            if (bilgi == "yeni"){
                binding.silButton.isEnabled = false
                binding.kaydetButton.isEnabled= true
                binding.malzemeText.setText("")
                binding.yemekIsimText.setText("")
            }else{
                binding.silButton.isEnabled = true
                binding.kaydetButton.isEnabled= false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun kaydet(view: View){

    }

    fun sil(view: View){

    }

    fun gorselSec(view: View){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                //izin verilmediyse
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_MEDIA_IMAGES)){
                    //snackbar ile neden izin istediğimizi söyleyip tekrar izin isteyeceğiz
                    Snackbar.make(view, "Galeriye ulaşıp foto veya görsel seçmemiz lazım!", Snackbar.LENGTH_INDEFINITE).setAction(
                        "İzin ver", View.OnClickListener {
                            //izin isteyeceğiz
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)

                        }).show()
                }else{
                    //izin isteyeceğiz
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }

            }else{
                //izin verilmiş
                val intentToGalery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGalery)
            }

        }else{
            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //izin verilmediyse
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)){
                    //snackbar ile neden izin istediğimizi söyleyip tekrar izin isteyeceğiz
                    Snackbar.make(view, "Galeriye ulaşıp foto veya görsel seçmemiz lazım!", Snackbar.LENGTH_INDEFINITE).setAction(
                        "İzin ver", View.OnClickListener {
                            //izin isteyeceğiz
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                        }).show()
                }else{
                    //izin isteyeceğiz
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }

            }else{
                //izin verilmiş
                val intentToGalery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGalery)
            }
        }
    }

    private fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    val intentFromResult = result.data
                    if (intentFromResult != null) {
                        secilenGorsel = intentFromResult.data
                        try {
                            if (Build.VERSION.SDK_INT >= 28) {
                                //yeni
                                val source = ImageDecoder.createSource(
                                    requireActivity().contentResolver,
                                    secilenGorsel!!
                                )
                                secilenBitMap = ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(secilenBitMap)
                            } else {
                                //eski
                                secilenBitMap = MediaStore.Images.Media.getBitmap(
                                    requireActivity().contentResolver,
                                    secilenGorsel
                                )
                                binding.imageView.setImageBitmap(secilenBitMap)
                            }
                        } catch (e: IOException) {

                        }
                    }
                }

            }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result ->
            if(result){
                //izin verildi, galeriye gidebiliriz
                val intentToGalery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGalery)
            }else{
                //izin verilmedi
                Toast.makeText(requireContext(),"İzin verilmedi", Toast.LENGTH_LONG).show()
            }
        }
    }

}
