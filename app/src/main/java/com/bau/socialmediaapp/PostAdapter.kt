package com.bau.socialmediaapp
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.bau.socialmediaapp.homeFragment.fragments.ProfileFragment
import com.squareup.picasso.Picasso;


//grid viewde kullancağımız adapter sınıfı
class PostAdapter(private val ıdd: String,private val context: Context, private val postList: List<gridModel>) : BaseAdapter() {

    private var layoutInflater: LayoutInflater? = null
    private lateinit var image: ImageView
    override fun getCount(): Int {
        return postList.size
    }
    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        if (layoutInflater == null) {
            layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }

        if (convertView == null) {
            convertView = layoutInflater!!.inflate(R.layout.gridview_item, null)
        }
        image = convertView!!.findViewById(R.id.image)
        val currentPost = postList[position]

        // Resmi yükle
        Picasso.get().load(currentPost.image).resize(180,400).into(image)

        // postun üstüne bastığımızda postscren ekranına gidiyor
        convertView.setOnClickListener {
            // Post ID'sini al
            val postId = currentPost.postId
            println("----------")
            println(postId)
            println(ıdd)

            // ekrana geçiş yaptığında bastığımız postuın idsini alıyor
            val intent = Intent(context, PostScreen::class.java)
            intent.putExtra("IDD", ıdd)
            intent.putExtra("postId", postId)
            context.startActivity(intent)
        }

        return convertView
    }



}