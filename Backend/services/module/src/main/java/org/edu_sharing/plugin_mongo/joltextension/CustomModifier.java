package org.edu_sharing.plugin_mongo.joltextension;

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.SpecDriven;
import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.common.tree.MatchedElement;
import com.bazaarvoice.jolt.common.tree.WalkedPath;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.modifier.OpMode;
import com.bazaarvoice.jolt.modifier.TemplatrSpecBuilder;
import com.bazaarvoice.jolt.modifier.function.Function;
import com.bazaarvoice.jolt.modifier.spec.ModifierCompositeSpec;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CustomModifier implements SpecDriven, ContextualTransform {

    private static final Map<String, Function> STOCK_FUNCTIONS = new HashMap<>();

    static {
        STOCK_FUNCTIONS.put("stringfy", new CustomFunction.stringfy());
        STOCK_FUNCTIONS.put("jsonfy", new CustomFunction.jsonfy());
        STOCK_FUNCTIONS.put("idFromNodeRef", new CustomFunction.idFromNodeRef());
        STOCK_FUNCTIONS.put("nodeRefFromId", new CustomFunction.nodeRefFromId());
    }

    private final ModifierCompositeSpec rootSpec;

    private CustomModifier( Object spec, OpMode opMode, Map<String, Function> functionsMap ) {
        if ( spec == null ){
            throw new SpecException( opMode.name() + " expected a spec of Map type, got 'null'." );
        }
        if ( ! ( spec instanceof Map ) ) {
            throw new SpecException( opMode.name() + " expected a spec of Map type, got " + spec.getClass().getSimpleName() );
        }

        if(functionsMap == null || functionsMap.isEmpty()) {
            throw new SpecException( opMode.name() + " expected a populated functions' map type, got " + (functionsMap == null?"null":"empty") );
        }

        functionsMap = Collections.unmodifiableMap( functionsMap );
        TemplatrSpecBuilder templatrSpecBuilder = new TemplatrSpecBuilder( opMode, functionsMap );
        rootSpec = new ModifierCompositeSpec( ROOT_KEY, (Map<String, Object>) spec, opMode, templatrSpecBuilder );
    }

    @Override
    public Object transform(Object input, Map<String, Object> context) {

        Map<String, Object> contextWrapper = new HashMap<>(  );
        contextWrapper.put( ROOT_KEY, context );

        MatchedElement rootLpe = new MatchedElement( ROOT_KEY );
        WalkedPath walkedPath = new WalkedPath();
        walkedPath.add( input, rootLpe );

        rootSpec.apply( ROOT_KEY, Optional.of( input), walkedPath, null, contextWrapper );
        return input;
    }

    /**
     * This variant of modifier creates the key/index is missing,
     * and overwrites the value if present
     */
    public static final class Overwritr extends CustomModifier {

        public Overwritr( Object spec ) {
            this( spec, STOCK_FUNCTIONS );
        }

        public Overwritr( Object spec, Map<String, Function> functionsMap ) {
            super( spec, OpMode.OVERWRITR, functionsMap );
        }
    }

    /**
     * This variant of modifier only writes when the key/index is missing
     */
    public static final class Definr extends CustomModifier {

        public Definr( final Object spec ) {
            this( spec, STOCK_FUNCTIONS );
        }

        public Definr( Object spec, Map<String, Function> functionsMap ) {
            super( spec, OpMode.DEFINER, functionsMap );
        }
    }

    /**
     * This variant of modifier only writes when the key/index is missing or the value is null
     */
    public static class Defaultr extends CustomModifier {

        public Defaultr( final Object spec ) {
            this( spec, STOCK_FUNCTIONS );
        }

        public Defaultr( Object spec, Map<String, Function> functionsMap ) {
            super( spec, OpMode.DEFAULTR, functionsMap );
        }
    }
}
