<%@ tag description="Training settings" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ attribute name="settingsType" required="true" %>
<c:choose>
    <%-- General settings --%>
    <c:when test="${settingsType == 'general'}">
        <table class="compact">
            <tbody>
            <tr>
                <td><p>The number of folds (= the number of models) to train</p></td>
                <td>
                    <div class="input-field">
                        <input id="training--n_folds" data-setting="--n_folds" type="number" value="5"/>
                        <label for="training--n_folds" data-type="int" data-error="Has to be integer">Default: 5 (Integer value)</label>
                    </div>
                </td>
            </tr>
            <tr>
                <td><p>Only train a single fold (= a single model)</p></td>
                <td>
                    <div class="input-field">
                        <input id="training--single_fold" data-setting="--single_fold" type="number" value=""/>
                        <label for="training--single_fold" data-type="int" data-error="Has to be integer">Default: - (train all folds)</label>
                    </div>
                </td>
            </tr>
            <tr>
                <td><p>Number of models to train in parallel</p></td>
                <td>
                    <div class="input-field">
                        <input id="training--max_parallel_models" data-setting="--max_parallel_models" type="number" />
                        <label for="training--max_parallel_models" data-type="int" data-error="Has to be integer">Default: -1 (Integer value) | Train all models in parallel</label>
                    </div>
                </td>
            </tr>
            <tr>
                <td>
                	<p>Whitelist of characters<br/>
                    <span class="userInfo">Will be kept even if they do not occur in the GT</span><br/>
                    <span class="userInfo">Example: ABCDEFGHIJ...012345</span>
                    </p>
                </td>
                <td>
                    <div class="input-field flex">
                        <!-- Temporary fix for faulty resizing of materialize-textarea -->
                        <input id="training--whitelist" data-setting="--codec.include_files" type="text" spellcheck="false"/>
                        <label for="training--whitelist">Default: A-Za-z0-9</label>
                        <a class="waves-effect waves-light dropdown-button btn whitelist-button" data-constrainWidth="false" href="#" data-activates="whitelist-select"><i class="material-icons">play_for_work</i></a>
                        <ul id="whitelist-select" class="dropdown-content"></ul>
                    </div>
                </td>
            </tr>
            <tr>
                <td><p>Keep codec of the loaded model(s)</p></td>
                <td>
                    <p>
                        <input type="checkbox" class="filled-in" data-setting="--codec.keep_loaded True" id="training--keep_loaded_codec" />
                        <label for="training--keep_loaded_codec"></label>
                    </p>
                </td>
            </tr>
            <tr>
                <td>
                    <p>Pretraining</p>
                    <span class="userInfo">It's highly advised to train based on one or more available models!</span><br/>
                    <span class="userWarning">Please note that if you're using a model with a deeper network structure ('deep3_' prefix) you have <br/>to adjust the network structure accordingly (see: 'Settings (Advanced)' → 'The network structure')</span>
                </td>
                <td>
                    <div class="input-field">
                        <i class="material-icons prefix">queue_play_next</i>
                        <select id="pretrainingType" name="pretrainingType" class="suffix ignoreParam">
                            <option value="from_scratch" selected>Train all models from scratch</option>
                            <option value="single_model">Train all models based on one available model</option>
                            <option value="multiple_models">Train each model based on different available models</option>
                        </select>
                        <label></label>
                    </div>
                </td>
            </tr>
                <%-- START Will be used to generate dropdown elements for each model --%>
            <tr id="pretrainingDummyTr" style="display: none;" data-id="pretrainingTr">
                <td><p>Pretraining for model: <span></span></p></td>
                <td>
                    <div class="input-field">
                        <select id="pretrainingDummySelect" name="pretrainingDummySelect" data-id="pretrainingModelSelect" class="ignoreParam">
                            <option value="None">Train model from scratch</option>
                        </select>
                        <label></label>
                    </div>
                </td>
            </tr>
            <tr>
                <td>
                    <p>
                        Data augmentation
                        <br />
                        <span class="userInfo">Number of data augmentations per line</span>
                    </p>
                </td>
                <td>
                    <div class="input-field">
                        <input id="training--n_augmentations" data-setting="--n_augmentations" type="number"/>
                        <label for="training--n_augmentations" data-type="int" data-error="Has to be integer">Default: 0 (Integer value)</label>
                    </div>
                </td>
            </tr>
                <%-- END Will be used to generate dropdown elements for each model --%>
            </tbody>
        </table>
    </c:when>
    <%-- Advanced settings --%>
    <c:when test="${settingsType == 'advanced'}">
        <table class="compact">
            <tbody>
            <tr>
                <td>
                    <p>
                        Early stopping
                        <br />
                        <span class="userInfo">The number of models that must be worse than the current best model to stop</span>
                    </p>
                </td>
                <td>
                    <div class="input-field">
                        <input id="training--early_stopping_nbest" data-setting="--early_stopping.n_to_go" type="number" value="5"/>
                        <label for="training--early_stopping_nbest" data-type="int" data-error="Has to be integer">Default: 10 (Integer value)</label>
                    </div>
                </td>
            </tr>
            <tr>
                <td><p>
                    Early stopping frequency
                    <br />
                    <span class="userInfo">Number of training steps between the evaluation of the current model</span>
                </p></td>
                <td>
                    <div class="input-field">
                        <input id="training--early_stopping_frequency" data-setting="--early_stopping.frequency" type="number"/>
                        <label for="training--early_stopping_frequency" data-type="int" data-error="Has to be integer">Default: # GT lines / 2 (Integer value)</label>
                    </div>
                </td>
            </tr>
            <tr>
                <td>
                    <p>
                        The number of iterations for training
                        <br />
                        <span class="userInfo">If using early stopping, this is the maximum number of iterations</span>
                    </p>
                </td>
                <td>
                    <div class="input-field">
                        <input id="training--max_iters" data-setting="--trainer.epochs" type="number" />
                        <label for="training--max_iters" data-type="int" data-error="Has to be integer">Default: 1000000 (Integer value)</label>
                    </div>
                </td>
            </tr>
            <tr>
                <td>
                    <p>
                        Training identifier
                        <br />
                        <span class="userInfo">A custom name can be used as identifier as well</span>
                    </p>
                </td>
                <td>
                    <div class="input-field">
                        <input id="trainingId" type="text" class="ignoreParam" />
                        <label for="trainingId">Default: Next unused incremented integer (0,1,2,...)</label>
                    </div>
                </td>
            </tr>
            <tr>
                <td>
                	<p>Load data on the fly instead of preloading it<br>
                        <span class="userInfo">Reduces RAM usage, increases processing time</span>
					</p>
				</td>
                <td>
                    <p>
                        <input type="checkbox" class="filled-in" data-setting="-train.preload True" id="training--train_data_on_the_fly" />
                        <label for="training--train_data_on_the_fly"></label>
                    </p>
                </td>
            </tr>
            <tr>
                <td><p>The batch size to use for training</p></td>
                <td>
                    <div class="input-field">
                        <input id="training--batch_size" data-setting="--train.batch_size" type="number" step="1" value="5"/>
                        <label for="training--batch_size" data-type="int" data-error="Has to be integer">Default: 5 (Integer value)</label>
                    </div>
                </td>
            </tr>
            <tr>
                <td><p>The line height</p></td>
                <td>
                    <div class="input-field">
                        <input id="training--line" data-setting="--data.line_height" type="number" />
                        <label for="training--line" data-type="int" data-error="Has to be integer">Default: 48 (Integer value)</label>
                    </div>
                </td>
            </tr>
            <tr>
                <td>
                    <p>
                        The network structure
                        <br />
                        <span class="userInfo">Default: cnn=40:3x3,pool=2x2,cnn=60:3x3,pool=2x2,lstm=200,dropout=0.5</span>
                        <br />
                        <span class="userInfo">deep3: cnn=40:3x3,pool=2x2,cnn=60:3x3,pool=2x2,cnn=120:3x3,lstm=200,lstm=200,lstm=200,dropout=0.5</span>
                    </p>
                </td>
                <td>
                    <div class="input-field">
                        <input id="training--network" data-setting="--network" type="text" />
                        <label for="training--network" >Default: See example in description</label>
                    </div>
                </td>
            </tr>
            <tr>
                <td>
                    <p>
                        Seed for random operations
                        <br />
                        <span class="userInfo">If negative or zero a 'random' seed is used</span>
                    </p>
                </td>
                <td>
                    <div class="input-field">
                        <input id="training--seed" data-setting="--trainer.random_seed" type="number" />
                        <label for="training--seed" data-type="int" data-error="Has to be integer">Default: 0</label>
                    </div>
                </td>
            </tr>
            <tr>
                <td><p>Do no skip invalid gt, instead raise an exception</p></td>
                <td>
                    <p>
                        <input type="checkbox" class="filled-in" data-setting="--data.skip_invalid False" id="training--no_skip_invalid_gt" />
                        <label for="training--no_skip_invalid_gt"></label>
                    </p>
                </td>
            </tr>

				<tr>
					<td><p>Estimate skew angle for every region without one</p></td>
					<td>
						<p>
							<input type="checkbox" class="filled-in" data-setting="--estimate_skew" id="training--estimate_skew" checked="checked"/>
							<label for="training--estimate_skew"></label>
						</p>
					</td>
				</tr>
				<tr>
					<td><p>Maximum estimated skew of a region</p></td>
					<td>
						<div class="input-field">
							<input id="training--maxskew" data-setting="--maxskew" type="number" step="0.001"/>
							<label for="training--maxskew" data-type="float" data-error="Has to be a float">Default: 2.0</label>
						</div>
					</td>
				</tr>
				<tr>
					<td><p>Steps between 0 and +/-maxskew to estimate the possible skew of a region.</p></td>
					<td>
						<div class="input-field">
							<input id="training--skewsteps" data-setting="--skewsteps" type="number" step="1"/>
							<label for="training--skewsteps" data-type="int" data-error="Has to be a float">Default: 8</label>
						</div>
					</td>
				</tr>
            </tbody>
        </table>
    </c:when>
</c:choose>
