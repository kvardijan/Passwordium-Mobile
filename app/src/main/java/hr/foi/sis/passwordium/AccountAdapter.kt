package hr.foi.sis.passwordium

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import hr.foi.sis.passwordium.models.AccountResponse
import hr.foi.sis.passwordium.models.EditAccountBody

class AccountAdapter(private val items: List<AccountResponse>, private val context: Activity) :
    RecyclerView.Adapter<AccountAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val websiteNameTextView: TextView = view.findViewById(R.id.tv_website_name)
        val usernameTextView: TextView = view.findViewById(R.id.tv_username)
        val urlTextView: TextView = view.findViewById(R.id.tv_url)
        val cardView: CardView = view.findViewById(R.id.cardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.account_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.websiteNameTextView.text = item.name
        holder.usernameTextView.text = item.username
        holder.urlTextView.text = if (item.url.isNotEmpty()) {
            item.url
        } else {
            ""
        }

        holder.cardView.setOnClickListener {
            copyPasswordToClipboard(item)
        }

        holder.cardView.setOnLongClickListener {
            val account = EditAccountBody(item.id,item.name,item.url,item.username,item.password)
            navigateToEditAccountActivity(account)
            true
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    private fun navigateToEditAccountActivity(accountItem: EditAccountBody) {
        val intent = Intent(context, EditAccountActivity::class.java)
        intent.putExtra("selectedAccountWebsiteName", accountItem.name)
        intent.putExtra("selectedAccountUsername", accountItem.username)
        intent.putExtra("selectedAccountUrl", accountItem.url)
        intent.putExtra("selectedAccountPassword",accountItem.password)
        intent.putExtra("selectedAccountId",accountItem.id.toString())
        context.startActivity(intent)
    }

    @SuppressLint("ServiceCast")
    private fun copyPasswordToClipboard(accountItem: AccountResponse) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("password", accountItem.password)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Password copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}

data class AccountItem(
    val websiteName: String,
    val username: String,
    val password: String,
    val url: String
)