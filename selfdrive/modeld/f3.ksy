meta:
  id: model_output
  endian: le

instances:
  plan_mhp_n:
    value: 5
  trajectory_size:
    value: 33
  lead_traj_len:
    value: 6
  lead_mhp_n:
    value: 2
  lead_mhp_selection:
    value: 3
  disengage_len:
    value: 5
  blinker_len:
    value: 6
  desire_pred_len:
    value: 4
    

seq:
  - id: plans
    type: model_output_plans
  - id: lane_lines
    type: model_output_lane_lines
  - id: road_edges
    type: model_output_road_edges
  - id: leads
    type: model_output_leads
  - id: meta
    type: model_output_meta
  - id: pose
    type: model_output_pose
  - id: wide_from_device_euler
    type: model_output_wide_from_device_euler
  - id: temporal_pose
    type: model_output_temporal_pose
  - id: road_transform
    type: model_output_road_transform
  - id: action
    type: lateral_action

types:
  model_output_xyz:
    seq:
      - id: x
        type: f4
      - id: y
        type: f4
      - id: z
        type: f4
  
  model_output_yz:
    seq:
      - id: y
        type: f4
      - id: z
        type: f4

  model_output_plan_element:
    seq:
      - id: position
        type: model_output_xyz
      - id: velocity
        type: model_output_xyz
      - id: acceleration
        type: model_output_xyz
      - id: rotation
        type: model_output_xyz
      - id: rotation_rate
        type: model_output_xyz

  model_output_plan_prediction:
    seq:
      - id: mean
        type: model_output_plan_element
        repeat: expr
        repeat-expr: _root.trajectory_size
      - id: std
        type: model_output_plan_element
        repeat: expr
        repeat-expr: _root.trajectory_size
      - id: prob
        type: f4

  model_output_plans:
    seq:
      - id: prediction
        type: model_output_plan_prediction
        repeat: expr
        repeat-expr: _root.plan_mhp_n

  model_output_lines_xy:
    seq:
      - id: left_far
        type: model_output_yz
        repeat: expr
        repeat-expr: _root.trajectory_size
      - id: left_near
        type: model_output_yz
        repeat: expr
        repeat-expr: _root.trajectory_size
      - id: right_near
        type: model_output_yz
        repeat: expr
        repeat-expr: _root.trajectory_size
      - id: right_far
        type: model_output_yz
        repeat: expr
        repeat-expr: _root.trajectory_size

  model_output_line_prob_val:
    seq:
      - id: val_deprecated
        type: f4
      - id: val
        type: f4

  model_output_lines_prob:
    seq:
      - id: left_far
        type: model_output_line_prob_val
      - id: left_near
        type: model_output_line_prob_val
      - id: right_near
        type: model_output_line_prob_val
      - id: right_far
        type: model_output_line_prob_val

  model_output_lane_lines:
    seq:
      - id: mean
        type: model_output_lines_xy
      - id: std
        type: model_output_lines_xy
      - id: prob
        type: model_output_lines_prob

  model_output_edgess_xy:
    seq:
      - id: left
        type: model_output_yz
        repeat: expr
        repeat-expr: _root.trajectory_size
      - id: right
        type: model_output_yz
        repeat: expr
        repeat-expr: _root.trajectory_size

  model_output_road_edges:
    seq:
      - id: mean
        type: model_output_edgess_xy
      - id: std
        type: model_output_edgess_xy

  model_output_lead_element:
    seq:
      - id: x
        type: f4
      - id: y
        type: f4
      - id: velocity
        type: f4
      - id: acceleration
        type: f4

  model_output_lead_prediction:
    seq:
      - id: mean
        type: model_output_lead_element
        repeat: expr
        repeat-expr: _root.lead_traj_len
      - id: std
        type: model_output_lead_element
        repeat: expr
        repeat-expr: _root.lead_traj_len
      - id: prob
        type: f4
        repeat: expr
        repeat-expr: _root.lead_mhp_selection

  model_output_leads:
    seq:
      - id: prediction
        type: model_output_lead_prediction
        repeat: expr
        repeat-expr: _root.lead_mhp_n
      - id: prob
        type: f4
        repeat: expr
        repeat-expr: _root.lead_mhp_selection

  model_output_pose:
    seq:
      - id: velocity_mean
        type: model_output_xyz
      - id: rotation_mean
        type: model_output_xyz
      - id: velocity_std
        type: model_output_xyz
      - id: rotation_std
        type: model_output_xyz

  model_output_wide_from_device_euler:
    seq:
      - id: mean
        type: model_output_xyz
      - id: std
        type: model_output_xyz

  model_output_temporal_pose:
    seq:
      - id: velocity_mean
        type: model_output_xyz
      - id: rotation_mean
        type: model_output_xyz
      - id: velocity_std
        type: model_output_xyz
      - id: rotation_std
        type: model_output_xyz

  model_output_road_transform:
    seq:
      - id: position_mean
        type: model_output_xyz
      - id: rotation_mean
        type: model_output_xyz
      - id: position_std
        type: model_output_xyz
      - id: rotation_std
        type: model_output_xyz

  model_output_disengage_prob:
    seq:
      - id: gas_disengage
        type: f4
      - id: brake_disengage
        type: f4
      - id: steer_override
        type: f4
      - id: brake_3ms2
        type: f4
      - id: brake_4ms2
        type: f4
      - id: brake_5ms2
        type: f4
      - id: gas_pressed
        type: f4

  model_output_blinker_prob:
    seq:
      - id: left
        type: f4
      - id: right
        type: f4

  model_output_desire_prob:
    seq:
      - id: none_value
        type: f4
      - id: turn_left
        type: f4
      - id: turn_right
        type: f4
      - id: lane_change_left
        type: f4
      - id: lane_change_right
        type: f4
      - id: keep_left
        type: f4
      - id: keep_right
        type: f4
      - id: null_value
        type: f4
    instances:
      raw:
        pos: 0
        type: f4
        repeat: expr
        repeat-expr: 8

  model_output_meta:
    seq:
      - id: desire_state_prob
        type: model_output_desire_prob
      - id: engaged_prob
        type: f4
      - id: disengage_prob
        type: model_output_disengage_prob
        repeat: expr
        repeat-expr: _root.disengage_len
      - id: blinker_prob
        type: model_output_blinker_prob
        repeat: expr
        repeat-expr: _root.blinker_len
      - id: desire_pred_prob
        type: model_output_desire_prob
        repeat: expr
        repeat-expr: _root.desire_pred_len

  lateral_action:
    seq:
      - id: desired_curvatures
        type: f4
