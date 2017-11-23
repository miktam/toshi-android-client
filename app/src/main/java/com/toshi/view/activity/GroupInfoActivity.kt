/*
 * 	Copyright (c) 2017. Toshi Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.toshi.view.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import com.toshi.R
import com.toshi.extensions.addHorizontalLineDivider
import com.toshi.extensions.toast
import com.toshi.model.local.Group
import com.toshi.model.local.User
import com.toshi.util.ImageUtil
import com.toshi.util.LogUtil
import com.toshi.view.adapter.GroupParticipantAdapter
import com.toshi.viewModel.GroupInfoViewModel
import kotlinx.android.synthetic.main.activity_group_info.*

class GroupInfoActivity : AppCompatActivity() {
    companion object {
        const val EXTRA__GROUP_ID = "extra_group_id"
    }

    private lateinit var userAdapter: GroupParticipantAdapter
    private lateinit var viewModel: GroupInfoViewModel

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_info)
        init()
    }

    private fun init() {
        initViewModel()
        initClickListeners()
        processIntentData()
        initObservers()
    }

    private fun initClickListeners() = closeButton.setOnClickListener { finish() }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(GroupInfoViewModel::class.java)
    }

    private fun processIntentData() {
        val groupId = getGroupIdFromIntent()
        groupId?.let { this.viewModel.fetchGroup(groupId) }
    }

    private fun getGroupIdFromIntent(): String? = intent.getStringExtra(EXTRA__GROUP_ID)

    private fun initObservers() {
        viewModel.group.observe(this, Observer {
            it?.let { updateView(it) }
        })
        viewModel.error.observe(this, Observer {
            LogUtil.exception(this::class.java, it)
            toast(R.string.error_unknown_group, Toast.LENGTH_LONG)
            finish()
        })
    }

    private fun updateView(group: Group) {
        groupName.text = group.title
        ImageUtil.load(group.avatar, avatar)
        initRecyclerView(group.members)
        initNumberOfParticipantsView(group.members)
    }

    private fun initRecyclerView(members: List<User>) {
        userAdapter = GroupParticipantAdapter().addUsers(members)
        participants.apply {
            layoutManager = LinearLayoutManager(this.context)
            itemAnimator = DefaultItemAnimator()
            adapter = userAdapter
            addHorizontalLineDivider()
        }
    }

    private fun initNumberOfParticipantsView(members: List<User>) {
        val participantSize = members.size
        val participantsLabel = this.resources.getQuantityString(R.plurals.participants, participantSize, participantSize)
        numberOfParticipants.text = participantsLabel
    }
}